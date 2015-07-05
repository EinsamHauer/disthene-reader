package net.iponweb.disthene.reader.format;

import com.google.common.base.Joiner;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.LogarithmicScaleNotAllowed;
import net.iponweb.disthene.reader.graph.LineGraph;
import net.iponweb.disthene.reader.handler.parameters.RenderParameters;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class ResponseFormatter {


    public static FullHttpResponse formatResponse(List<TimeSeries> timeSeriesList, RenderParameters parameters) throws NotImplementedException, LogarithmicScaleNotAllowed {
        switch (parameters.getFormat()) {
            case JSON: return formatResponseAsJson(timeSeriesList);
            case RAW: return formatResponseAsRaw(timeSeriesList);
            case PNG: return formatResponseAsPng(timeSeriesList, parameters);
            default:throw new NotImplementedException();
        }
    }

    private static FullHttpResponse formatResponseAsRaw(List<TimeSeries> timeSeriesList) {
        List<String> results = new ArrayList<>();

        for(TimeSeries timeSeries : timeSeriesList) {
            results.add(timeSeries.getName() + "," + timeSeries.getFrom() + "," + timeSeries.getTo() + "," + timeSeries.getStep() + "|" + Joiner.on(",").useForNull("null").join(timeSeries.getValues()));
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

    private static FullHttpResponse formatResponseAsJson(List<TimeSeries> timeSeriesList) {
        List<String> results = new ArrayList<>();

        for(TimeSeries timeSeries : timeSeriesList) {
            List<String> datapoints = new ArrayList<>();
            for(int i = 0; i < timeSeries.getValues().length; i++) {
                datapoints.add("[" + timeSeries.getValues()[i] + ", " + (timeSeries.getFrom() + timeSeries.getStep() * i) + "]");
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
                Unpooled.wrappedBuffer(new LineGraph(renderParameters, timeSeriesList).drawGraph()));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "image/png");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }
}
