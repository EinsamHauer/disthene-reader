package net.iponweb.disthene.reader.graph;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.beans.TimeSeriesOption;
import net.iponweb.disthene.reader.exceptions.LogarithmicScaleNotAllowed;
import net.iponweb.disthene.reader.handler.parameters.ImageParameters;
import net.iponweb.disthene.reader.handler.parameters.RenderParameters;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class LineGraph extends Graph {

    public LineGraph(RenderParameters renderParameters, List<TimeSeries> data) {
        super(renderParameters, data);
    }

    @Override
    public byte[] drawGraph() throws LogarithmicScaleNotAllowed {
        if (data.size() == 0) {
            return drawNoData();
        }

        for (DecoratedTimeSeries ts : data) {
            startTime = Math.min(startTime, ts.getFrom());
            endTime = Math.max(endTime, ts.getTo());

            if (ts.hasOption(TimeSeriesOption.SECOND_Y_AXIS)) {
                secondYAxis = true;
                dataRight.add(ts);
                if (imageParameters.getRightColor() != null) ts.setOption(TimeSeriesOption.COLOR, imageParameters.getRightColor());
                //todo dash length constant
                if (imageParameters.getRightDashed()) ts.setOption(TimeSeriesOption.DASHED, (float) 5);
                if (imageParameters.getRightWidth() != null) ts.setOption(TimeSeriesOption.LINE_WIDTH, imageParameters.getRightWidth().floatValue());
            } else {
                dataLeft.add(ts);
                if (imageParameters.getLeftColor() != null) ts.setOption(TimeSeriesOption.COLOR, imageParameters.getLeftColor());
                //todo dash length constant
                if (imageParameters.getLeftDashed()) ts.setOption(TimeSeriesOption.DASHED, (float) 5);
                if (imageParameters.getLeftWidth() != null) ts.setOption(TimeSeriesOption.LINE_WIDTH, imageParameters.getLeftWidth().floatValue());
            }
        }

        // Set stacked options where needed right away
        if ((imageParameters.getAreaMode().equals(ImageParameters.AreaMode.STACKED) || imageParameters.getAreaMode().equals(ImageParameters.AreaMode.ALL)) && !secondYAxis) {
            for (DecoratedTimeSeries ts : data) {
                if (!ts.hasOption(TimeSeriesOption.DRAW_AS_INFINITE)) {
                    ts.addOption(TimeSeriesOption.STACKED);
                }
            }
        } else if (imageParameters.getAreaMode().equals(ImageParameters.AreaMode.FIRST) && !secondYAxis) {
            if (data.size() > 0) {
                data.get(0).addOption(TimeSeriesOption.STACKED);
            }
        }

        int length = data.get(0).getValues().length;
        double[] total = new double[length];
        for (DecoratedTimeSeries ts : getStackedData(data)) {
            for (int i = 0; i < length; i++) {
                if (ts.getValues()[i] != null) {
                    double original = ts.getValues()[i];
                    ts.getValues()[i] += total[i];
                    total[i] += original;
                }
            }
        }


        if (imageParameters.isGraphOnly()) {
            imageParameters.setHideLegend(true);
            imageParameters.setHideGrid(true);
            imageParameters.setHideAxes(true);
            imageParameters.setHideYAxis(false);

            imageParameters.setyAxisSide(ImageParameters.Side.LEFT);
            imageParameters.setTitle("");
            imageParameters.setVerticalTitle("");
            imageParameters.setMargin(0);
// todo: ??            params['tz'] = ''

            xMin = 0;
            xMax = imageParameters.getWidth();
            yMin = 0;
            yMax = imageParameters.getHeight();
        }

        if (secondYAxis) {
            imageParameters.setyAxisSide(ImageParameters.Side.LEFT);
        }

        if (imageParameters.getLineMode().equals(ImageParameters.LineMode.SLOPE)) {
            for (DecoratedTimeSeries ts : data) {
                if (ts.getValues().length <= 1) {
                    imageParameters.setLineMode(ImageParameters.LineMode.STAIRCASE);
                }
            }
        }

        //assign colors
        int i = 0;
        for (DecoratedTimeSeries ts : data) {
            if (!ts.hasOption(TimeSeriesOption.COLOR)) {
                ts.setOption(TimeSeriesOption.COLOR, imageParameters.getColorList().get(i % imageParameters.getColorList().size()));
                i++;
            }
        }

        if (!imageParameters.getTitle().isEmpty()) {
            drawTitle();
        }

        if (!imageParameters.getVerticalTitle().isEmpty()) {
            drawVerticalTitle(false);
        }

        if (secondYAxis && !imageParameters.getVerticalTitleRight().isEmpty()) {
            drawVerticalTitle(true);
        }

        //todo: config legend max items
        if (!imageParameters.isHideLegend() && (data.size() <= 10)) {
            List<String> legends = new ArrayList<>();
            List<Color> colors = new ArrayList<>();
            List<Boolean> secondYAxes = new ArrayList<>();

            for (DecoratedTimeSeries ts : data) {
                legends.add(ts.getName());
                colors.add((Color) ts.getOption(TimeSeriesOption.COLOR));
                secondYAxes.add(ts.hasOption(TimeSeriesOption.SECOND_Y_AXIS));
            }

            drawLegend(legends, colors, secondYAxes, imageParameters.isUniqueLegend());
        }

        if (!imageParameters.isHideAxes()) {
            FontMetrics fontMetrics = g2d.getFontMetrics(imageParameters.getFont());
            yMax -= fontMetrics.getMaxAscent() * 2;
        }

        consolidateDataPoints();

        int currentXMin = xMin;
        int currentXMax = xMax;

        if (secondYAxis) {
            setupTwoYAxes();
        } else {
            setupYAxis();
        }

        while (currentXMin != xMin || currentXMax != xMax) {
            consolidateDataPoints();
            currentXMin = xMin;
            currentXMax = xMax;

            if (secondYAxis) {
                setupTwoYAxes();
            } else {
                setupYAxis();
            }
        }

        setupXAxis();

        if (!imageParameters.isHideAxes()) {
            drawLabels();
            if (!imageParameters.isHideGrid()) {
                drawGridLines();
            }
        }

        drawData();

        return getBytes();
    }

    private byte[] drawNoData() {
        int x = imageParameters.getWidth() / 2;
        int y = imageParameters.getHeight() / 2;

        g2d.setPaint(ColorTable.RED);
        Font font = new Font(imageParameters.getFont().getName(), imageParameters.getFont().getStyle(),
                (int) Math.log(imageParameters.getHeight() * imageParameters.getWidth()));

        drawText(x, y, "No Data", font, ColorTable.RED, HorizontalAlign.CENTER, VerticalAlign.TOP);

        return getBytes();
    }

}
