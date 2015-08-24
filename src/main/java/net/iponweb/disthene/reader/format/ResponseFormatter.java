package net.iponweb.disthene.reader.format;

import com.google.common.base.Joiner;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.LogarithmicScaleNotAllowed;
import net.iponweb.disthene.reader.graph.DecoratedTimeSeries;
import net.iponweb.disthene.reader.graph.Graph;
import net.iponweb.disthene.reader.graphite.utils.GraphiteUtils;
import net.iponweb.disthene.reader.handler.parameters.RenderParameters;
import org.joda.time.DateTime;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class ResponseFormatter {


    public static FullHttpResponse formatResponse(List<TimeSeries> timeSeriesList, RenderParameters parameters) throws NotImplementedException, LogarithmicScaleNotAllowed {
        // Let's remove empty series
        List<TimeSeries> filtered = filterAllNulls(timeSeriesList);

        switch (parameters.getFormat()) {
            case JSON: return formatResponseAsJson(filtered, parameters);
            case RAW: return formatResponseAsRaw(filtered);
            case CSV: return formatResponseAsCSV(filtered, parameters);
            case PNG: return formatResponseAsPng(filtered, parameters);
            default:throw new NotImplementedException();
        }
    }

    private static FullHttpResponse formatResponseAsCSV(List<TimeSeries> timeSeriesList, RenderParameters renderParameters) {
        List<String> results = new ArrayList<>();

        for(TimeSeries timeSeries : timeSeriesList) {
            Double[] values = timeSeries.getValues();
            for(int i = 0; i < values.length; i++) {
                DateTime dt = new DateTime((timeSeries.getFrom() + i * timeSeries.getStep()) * 1000, renderParameters.getTz());
                String stringValue;
                if (values[i] == null) {
                    stringValue = "";
                } else {
                    BigDecimal bigDecimal = BigDecimal.valueOf(values[i]);
                    if (bigDecimal.precision() > 10) {
                        bigDecimal = bigDecimal.setScale(bigDecimal.precision() - 1, BigDecimal.ROUND_HALF_UP);
                    }

                    stringValue = bigDecimal.stripTrailingZeros().toPlainString();
                }
                results.add(timeSeries.getName() + "," + dt.toString("YYYY-MM-dd HH:mm:ss") + "," + stringValue);
            }
        }

        String responseString = Joiner.on("\n").join(results);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseString.getBytes()));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/csv");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    private static FullHttpResponse formatResponseAsRaw(List<TimeSeries> timeSeriesList) {
        List<String> results = new ArrayList<>();

        for(TimeSeries timeSeries : timeSeriesList) {
            List<String> formattedValues = new ArrayList<>();
            for (Double value : timeSeries.getValues()) {
                if (value == null) {
                    formattedValues.add("null");
                } else {
                    formattedValues.add(GraphiteUtils.formatDoubleSpecialPlain(value));
                }
            }
            results.add(timeSeries.getName() + "," + timeSeries.getFrom() + "," + timeSeries.getTo() + "," + timeSeries.getStep() + "|" + Joiner.on(",").useForNull("null").join(formattedValues));
        }

        String responseString = Joiner.on("\n").join(results);


        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseString.getBytes()));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    private static FullHttpResponse formatResponseAsJson(List<TimeSeries> timeSeriesList, RenderParameters renderParameters) {
        List<String> results = new ArrayList<>();

        // consolidate data points
        consolidate(timeSeriesList, renderParameters.getMaxDataPoints());

        for(TimeSeries timeSeries : timeSeriesList) {
            List<String> datapoints = new ArrayList<>();
            for(int i = 0; i < timeSeries.getValues().length; i++) {
                String stringValue;
                if (timeSeries.getValues()[i] == null) {
                    stringValue = "null";
                } else {
                    stringValue = GraphiteUtils.formatDoubleSpecialPlain(timeSeries.getValues()[i]);
                }

                datapoints.add("[" + stringValue + ", " + (timeSeries.getFrom() + timeSeries.getStep() * i) + "]");
            }
            results.add("{\"target\": \"" + timeSeries.getName() + "\", \"datapoints\": [" + Joiner.on(", ").join(datapoints) + "]}");
        }
        String responseString = "[" + Joiner.on(", ").join(results) + "]";


        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseString.getBytes()));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    private static FullHttpResponse formatResponseAsPng(List<TimeSeries> timeSeriesList, RenderParameters renderParameters) throws LogarithmicScaleNotAllowed {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(Graph.getInstance(renderParameters.getImageParameters().getGraphType(), renderParameters, timeSeriesList).drawGraph()));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "image/png");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    private static void consolidate(List<TimeSeries> timeSeriesList, int maxDataPoints) {
        for (TimeSeries ts : timeSeriesList) {
            DecoratedTimeSeries dts = new DecoratedTimeSeries(ts);
            if (maxDataPoints < ts.getValues().length) {
                dts.setValuesPerPoint((int) Math.ceil(ts.getValues().length / (double) maxDataPoints));
            } else {
                dts.setValuesPerPoint(1);
            }

            ts.setStep(dts.getValuesPerPoint() * dts.getStep());
            ts.setTo(ts.getFrom() + ts.getStep() * dts.getConsolidatedValues().length - 1);
            ts.setValues(dts.getConsolidatedValues());
        }
    }

    private static List<TimeSeries> filterAllNulls(List<TimeSeries> timeSeriesList) {
        List<TimeSeries> result = new ArrayList<>();

        for (TimeSeries ts : timeSeriesList) {
            for (Double value : ts.getValues()) {
                if (value != null) {
                    result.add(ts);
                    break;
                }
            }
        }

        return result;
    }
}
