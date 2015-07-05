package net.iponweb.disthene.reader.graph;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.beans.TimeSeriesOption;
import net.iponweb.disthene.reader.exceptions.LogarithmicScaleNotAllowed;
import net.iponweb.disthene.reader.handler.parameters.ImageParameters;
import net.iponweb.disthene.reader.handler.parameters.RenderParameters;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * @author Andrei Ivanov
 *         <p/>
 *         This class and those below in hierarchy are pure translations from graphite-web Python code.
 *         This will probably changed some day. But for now reverse engineering the logic is too comaplicated.
 */
public abstract class Graph {
    final static Logger logger = Logger.getLogger(Graph.class);

    private static final double[] PRETTY_VALUES = {0.1, 0.2, 0.25, 0.5, 1.0, 1.2, 1.25, 1.5, 2.0, 2.25, 2.5};

    protected ImageParameters imageParameters;
    protected RenderParameters renderParameters;

    protected List<DecoratedTimeSeries> data = new ArrayList<>();
    protected List<DecoratedTimeSeries> dataLeft = new ArrayList<>();
    protected List<DecoratedTimeSeries> dataRight = new ArrayList<>();
    protected boolean secondYAxis = false;

    protected int xMin;
    protected int xMax;
    protected int yMin;
    protected int yMax;

    protected int graphWidth;
    protected int graphHeight;

    protected long startTime = Long.MAX_VALUE;
    protected long endTime = Long.MIN_VALUE;

    protected DateTime startDateTime;
    protected DateTime endDateTime;

    protected double yStep;
    protected double yBottom;
    protected double yTop;
    protected double ySpan;
    protected double yScaleFactor;

    protected double yStepL;
    protected double yStepR;
    protected double yBottomL;
    protected double yBottomR;
    protected double yTopL;
    protected double yTopR;
    protected double ySpanL;
    protected double ySpanR;
    protected double yScaleFactorL;
    protected double yScaleFactorR;

    protected List<Double> yLabelValues;
    protected List<String> yLabels;
    protected int yLabelWidth;

    protected List<Double> yLabelValuesL;
    protected List<Double> yLabelValuesR;
    protected List<String> yLabelsL;
    protected List<String> yLabelsR;
    protected int yLabelWidthL;
    protected int yLabelWidthR;

    protected double xScaleFactor;
    protected XAxisConfig xAxisConfig;
    protected long xLabelStep;
    protected long xMinorGridStep;
    protected long xMajorGridStep;

    protected BufferedImage image;
    protected Graphics2D g2d;


    public Graph(RenderParameters renderParameters, List<TimeSeries> data) {
        this.renderParameters = renderParameters;
        this.imageParameters = renderParameters.getImageParameters();

        for (TimeSeries ts : data) {
            this.data.add(new DecoratedTimeSeries(ts));
        }

        xMin = imageParameters.getMargin() + 10;
        xMax = imageParameters.getWidth() - imageParameters.getMargin();
        yMin = imageParameters.getMargin();
        yMax = imageParameters.getHeight() - imageParameters.getMargin();


        image = new BufferedImage(imageParameters.getWidth(), imageParameters.getHeight(), BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        g2d.setPaint(imageParameters.getBackgroundColor());
        g2d.fillRect(0, 0, imageParameters.getWidth(), imageParameters.getHeight());
    }

    public abstract byte[] drawGraph() throws LogarithmicScaleNotAllowed;

    protected byte[] getBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error(e);
            return new byte[0];
        }

    }

    protected void drawText(int x, int y, String text, HorizontalAlign horizontalAlign, VerticalAlign verticalAlign) {
        drawText(x, y, text, imageParameters.getFont(), imageParameters.getForegroundColor(), horizontalAlign, verticalAlign, 0);
    }

    protected void drawText(int x, int y, String text, Font font, Color color, HorizontalAlign horizontalAlign, VerticalAlign verticalAlign) {
        drawText(x, y, text, font, color, horizontalAlign, verticalAlign, 0);
    }

    protected void drawText(int x, int y, String text, Font font, Color color, HorizontalAlign horizontalAlign, VerticalAlign verticalAlign, int rotate) {
        g2d.setPaint(color);
        g2d.setFont(font);

        FontMetrics fontMetrics = g2d.getFontMetrics(font);
        int textWidth = fontMetrics.stringWidth(text);
        int horizontal, vertical;

        switch (horizontalAlign) {
            case RIGHT:
                horizontal = textWidth;
                break;
            case CENTER:
                horizontal = textWidth / 2;
                break;
            default:
                horizontal = 0;
                break;
        }

        switch (verticalAlign) {
            case MIDDLE:
                vertical = fontMetrics.getHeight() / 2 - fontMetrics.getDescent();
                break;
            case BOTTOM:
                vertical = -fontMetrics.getDescent();
                break;
            case BASELINE:
                vertical = 0;
                break;
            default:
                vertical = fontMetrics.getAscent();
        }

        double angle = Math.toRadians(rotate);

        AffineTransform orig = g2d.getTransform();
        g2d.rotate(angle, x, y);

        g2d.drawString(text, x - horizontal, y + vertical);

        g2d.setTransform(orig);

    }

    protected void drawTitle() {
        int y = yMin;
        int x = imageParameters.getWidth() / 2;

        Font font = new Font(imageParameters.getFont().getName(), imageParameters.getFont().getStyle(),
                (int) (imageParameters.getFont().getSize() + Math.log(imageParameters.getFont().getSize())));

        FontMetrics fontMetrics = g2d.getFontMetrics(font);
        int lineHeight = fontMetrics.getHeight();

        String[] split = imageParameters.getTitle().split("\n");

        for (String line : split) {
            drawText(x, y, line, font, imageParameters.getForegroundColor(), HorizontalAlign.CENTER, VerticalAlign.TOP);
            y += lineHeight;
        }

        if (imageParameters.getyAxisSide().equals(ImageParameters.Side.RIGHT)) {
            yMin = y;
        } else {
            yMin = y + imageParameters.getMargin();
        }
    }

    protected void drawLegend(List<String> legends, List<Color> colors, List<Boolean> secondYAxes, boolean uniqueLegend) {

        // remove duplicate names
        List<String> legendsUnique = new ArrayList<>();
        List<Color> colorsUnique = new ArrayList<>();
        List<Boolean> secondYAxesUnique = new ArrayList<>();

        if (uniqueLegend) {
            for (int i = 0; i < legends.size(); i++) {
                if (!legendsUnique.contains(legends.get(i))) {
                    legendsUnique.add(legends.get(i));
                    colorsUnique.add(colors.get(i));
                    secondYAxesUnique.add(secondYAxes.get(i));
                }

            }

            legends = legendsUnique;
            colors = colorsUnique;
            secondYAxes = secondYAxesUnique;
        }

        FontMetrics fontMetrics = g2d.getFontMetrics(imageParameters.getFont());


        // Check if there's enough room to use two columns
        boolean rightSideLabels = false;
        int padding = 5;
        String longestLegend = Collections.max(legends);
        // Double it to check if there's enough room for 2 columns
        String testSizeName = longestLegend + " " + longestLegend;
        int testBoxSize = fontMetrics.getHeight() - 1;
        int testWidth = fontMetrics.stringWidth(testSizeName) + 2 * (testBoxSize + padding);

        if (testWidth + 50 < imageParameters.getWidth()) {
            rightSideLabels = true;
        }

        if (secondYAxis && rightSideLabels) {
            int boxSize = fontMetrics.getHeight() - 1;
            int lineHeight = fontMetrics.getHeight() + 1;
            int labelWidth = fontMetrics.stringWidth(longestLegend) + 2 * (boxSize + padding);
            int columns = (int) Math.max(1, Math.floor((imageParameters.getWidth() - xMin) / labelWidth));
            if (columns < 1) columns = 1;
            int numRight = 0;
            for (Boolean b : secondYAxes) {
                if (b) numRight++;
            }
            int numberOfLines = Math.max(legends.size() - numRight, numRight);
            columns = (int) Math.floor(columns / 2.0);
            int legendHeight = Math.max(1, (numberOfLines / columns)) * (lineHeight + padding);
            yMax -= legendHeight;
            int x = xMin;
            int y = yMax + 2 * padding;
            int n = 0;
            int xRight = xMax - xMin;
            int yRight = y;
            int nRight = 0;

            for (int i = 0; i < legends.size(); i++) {
                g2d.setPaint(colors.get(i));
                if (secondYAxes.get(i)) {
                    nRight++;
                    g2d.fillRect(xRight - padding, yRight, boxSize, boxSize);
                    g2d.setPaint(new Color(111, 111, 111));
                    g2d.drawRect(xRight - padding, yRight, boxSize, boxSize);
                    drawText(xRight - boxSize, yRight, legends.get(i), imageParameters.getFont(), imageParameters.getForegroundColor(), HorizontalAlign.RIGHT, VerticalAlign.TOP);
                    xRight -= labelWidth;

                    if (nRight % columns == 0) {
                        xRight = xMax - xMin;
                        yRight += lineHeight;
                    }
                } else {
                    n++;
                    g2d.fillRect(x, y, boxSize, boxSize);
                    g2d.setPaint(new Color(111, 111, 111));
                    g2d.drawRect(x, y, boxSize, boxSize);
                    drawText(x + boxSize + padding, y, legends.get(i), imageParameters.getFont(), imageParameters.getForegroundColor(), HorizontalAlign.LEFT, VerticalAlign.TOP);
                    x += labelWidth;

                    if (n % columns == 0) {
                        x = xMin;
                        y += lineHeight;
                    }
                }

            }
        } else {
            int boxSize = fontMetrics.getHeight() - 1;
            int lineHeight = fontMetrics.getHeight() + 1;
            int labelWidth = fontMetrics.stringWidth(longestLegend) + 2 * (boxSize + padding);
            int columns = (int) Math.floor(imageParameters.getWidth() / labelWidth);
            if (columns < 1) columns = 1;
            int numberOfLines = (int) Math.ceil((double) legends.size() / columns);
            int legendHeight = numberOfLines * (lineHeight + padding);
            yMax -= legendHeight;

            g2d.setStroke(new BasicStroke(1f));

            int x = xMin;
            int y = yMax + (2 * padding);
            for (int i = 0; i < legends.size(); i++) {
                if (secondYAxes.get(i)) {
                    g2d.setPaint(colors.get(i));
                    g2d.fillRect(x + labelWidth + padding, y, boxSize, boxSize);
                    g2d.setPaint(new Color(111, 111, 111));
                    g2d.drawRect(x + labelWidth + padding, y, boxSize, boxSize);
                    drawText(x + labelWidth, y, legends.get(i), imageParameters.getFont(), imageParameters.getForegroundColor(), HorizontalAlign.RIGHT, VerticalAlign.TOP);
                    x += labelWidth;
                } else {
                    g2d.setPaint(colors.get(i));
                    g2d.fillRect(x, y, boxSize, boxSize);
                    // todo; use color dictionary
                    g2d.setPaint(new Color(111, 111, 111));
                    g2d.drawRect(x, y, boxSize, boxSize);
                    drawText(x + boxSize + padding, y, legends.get(i), imageParameters.getFont(), imageParameters.getForegroundColor(), HorizontalAlign.LEFT, VerticalAlign.TOP);
                    x += labelWidth;
                }
                if ((i + 1) % columns == 0) {
                    x = xMin;
                    y += lineHeight;
                }
            }
        }
    }

    protected void consolidateDataPoints() {
        int numberOfPixels = (int) (xMax - xMin - imageParameters.getLineWidth() - 1);
        graphWidth = (int) (xMax - xMin - imageParameters.getLineWidth() - 1);

        for (DecoratedTimeSeries ts : data) {
            double numberOfDataPoints = ts.getValues().length;
            double divisor = ts.getValues().length;
            double bestXStep = numberOfPixels / divisor;

            if (bestXStep < imageParameters.getMinXStep()) {
                int drawableDataPoints = numberOfPixels / imageParameters.getMinXStep();
                double pointsPerPixel = Math.ceil(numberOfDataPoints / drawableDataPoints);
                ts.setValuesPerPoint((int) pointsPerPixel);
                ts.setxStep((numberOfPixels * pointsPerPixel) / numberOfDataPoints);
            } else {
                ts.setxStep(bestXStep);
            }

        }
    }

    protected void setupTwoYAxes() throws LogarithmicScaleNotAllowed {
        List<DecoratedTimeSeries> lData = new ArrayList<>();
        List<DecoratedTimeSeries> rData = new ArrayList<>();
        lData.addAll(dataLeft);
        rData.addAll(dataRight);
        List<DecoratedTimeSeries> seriesWithMissingValuesL = new ArrayList<>();
        List<DecoratedTimeSeries> seriesWithMissingValuesR = new ArrayList<>();

        for (DecoratedTimeSeries ts : dataLeft) {
            for (Double value : ts.getValues()) {
                if (value == null) {
                    seriesWithMissingValuesL.add(ts);
                    break;
                }
            }
        }

        for (DecoratedTimeSeries ts : dataRight) {
            for (Double value : ts.getValues()) {
                if (value == null) {
                    seriesWithMissingValuesR.add(ts);
                    break;
                }
            }
        }

        double yMinValueL = Double.POSITIVE_INFINITY;
        double yMinValueR = Double.POSITIVE_INFINITY;
        double yMaxValueL;
        double yMaxValueR;

        if (imageParameters.isDrawNullAsZero() && seriesWithMissingValuesL.size() > 0) {
            yMinValueL = 0;
        } else {
            for (DecoratedTimeSeries ts : dataLeft) {
                if (!ts.hasOption(TimeSeriesOption.DRAW_AS_INFINITE)) {
                    double mm = GraphUtils.safeMin(ts);
                    yMinValueL = mm < yMinValueL ? mm : yMinValueL;
                }
            }
        }

        if (imageParameters.isDrawNullAsZero() && seriesWithMissingValuesR.size() > 0) {
            yMinValueR = 0;
        } else {
            for (DecoratedTimeSeries ts : dataRight) {
                if (!ts.hasOption(TimeSeriesOption.DRAW_AS_INFINITE)) {
                    double mm = GraphUtils.safeMin(ts);
                    yMinValueR = mm < yMinValueR ? mm : yMinValueR;
                }
            }
        }

        if (imageParameters.getAreaMode().equals(ImageParameters.AreaMode.STACKED)) {
            yMaxValueL = GraphUtils.maxSum(dataLeft);
            yMaxValueR = GraphUtils.maxSum(dataRight);
        } else {
            yMaxValueL = GraphUtils.safeMax(dataLeft);
            yMaxValueR = GraphUtils.safeMax(dataRight);
        }

        if (yMinValueL == Double.POSITIVE_INFINITY) {
            yMinValueL = 0.0;
        }
        if (yMinValueR == Double.POSITIVE_INFINITY) {
            yMinValueR = 0.0;
        }

        if (imageParameters.getyMaxLeft() < Double.POSITIVE_INFINITY) {
            yMaxValueL = imageParameters.getyMaxLeft();
        }
        if (imageParameters.getyMaxRight() < Double.POSITIVE_INFINITY) {
            yMaxValueR = imageParameters.getyMaxRight();
        }
        if (imageParameters.getyMinLeft() > Double.NEGATIVE_INFINITY) {
            yMinValueL = imageParameters.getyMinLeft();
        }
        if (imageParameters.getyMinRight() > Double.NEGATIVE_INFINITY) {
            yMinValueR = imageParameters.getyMinRight();
        }
        if (yMaxValueL <= yMinValueL) {
            yMaxValueL = yMinValueL + 1;
        }
        if (yMaxValueR <= yMinValueR) {
            yMaxValueR = yMinValueR + 1;
        }

        double yVarianceL = yMaxValueL - yMinValueL;
        double yVarianceR = yMaxValueR - yMinValueR;
        double orderL = Math.log10(yVarianceL);
        double orderR = Math.log10(yVarianceR);
        double orderFactorL = Math.pow(10, Math.floor(orderL));
        double orderFactorR = Math.pow(10, Math.floor(orderR));
        double vL = yVarianceL / orderFactorL;
        double vR = yVarianceR / orderFactorR;

        double distance = Double.POSITIVE_INFINITY;
        double prettyValueL = PRETTY_VALUES[0];
        double prettyValueR = PRETTY_VALUES[0];
        for (int i = 0; i < imageParameters.getyDivisors().size(); i++) {
            double q = vL / imageParameters.getyDivisors().get(i);
            double p = GraphUtils.closest(q, PRETTY_VALUES);

            if (Math.abs(q - p) < distance) {
                distance = Math.abs(q - p);
                prettyValueL = p;
            }
        }

        distance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < imageParameters.getyDivisors().size(); i++) {
            double q = vR / imageParameters.getyDivisors().get(i);
            double p = GraphUtils.closest(q, PRETTY_VALUES);

            if (Math.abs(q - p) < distance) {
                distance = Math.abs(q - p);
                prettyValueR = p;
            }
        }

        yStepL = prettyValueL * orderFactorL;
        yStepR = prettyValueR * orderFactorR;

        if (imageParameters.getyStepLeft() < Double.POSITIVE_INFINITY) {
            yStepL = imageParameters.getyStepLeft();
        }
        if (imageParameters.getyStepRight() < Double.POSITIVE_INFINITY) {
            yStepR = imageParameters.getyStepRight();
        }

        yBottomL = yStepL * Math.floor(yMinValueL / yStepL);
        yBottomR = yStepR * Math.floor(yMinValueR / yStepR);
        yTopL = yStepL * Math.ceil(yMaxValueL / yStepL);
        yTopR = yStepR * Math.ceil(yMaxValueR / yStepR);

        if (imageParameters.getLogBase() != 0 && yMaxValueL > 0) {
            yBottomL = Math.pow(imageParameters.getLogBase(), Math.floor(Math.log(yMinValueL) / Math.log(imageParameters.getLogBase())));
            yTopL = Math.pow(imageParameters.getLogBase(), Math.ceil(Math.log(yMaxValueL) / Math.log(imageParameters.getLogBase())));
        } else if (imageParameters.getLogBase() != 0 && yMinValueL <= 0) {
            throw new LogarithmicScaleNotAllowed("Logarithmic scale specified with a dataset with a minimum value less than or equal to zero");
        }
        if (imageParameters.getLogBase() != 0 && yMaxValueR > 0) {
            yBottomR = Math.pow(imageParameters.getLogBase(), Math.floor(Math.log(yMinValueR) / Math.log(imageParameters.getLogBase())));
            yTopR = Math.pow(imageParameters.getLogBase(), Math.ceil(Math.log(yMaxValueR) / Math.log(imageParameters.getLogBase())));
        } else if (imageParameters.getLogBase() != 0 && yMinValueR <= 0) {
            throw new LogarithmicScaleNotAllowed("Logarithmic scale specified with a dataset with a minimum value less than or equal to zero");
        }

        if (imageParameters.getyMaxLeft() < Double.POSITIVE_INFINITY) {
            yTopL = imageParameters.getyMaxLeft();
        }
        if (imageParameters.getyMaxRight() < Double.POSITIVE_INFINITY) {
            yTopR = imageParameters.getyMaxRight();
        }
        if (imageParameters.getyMinLeft() > Double.NEGATIVE_INFINITY) {
            yBottomL = imageParameters.getyMinLeft();
        }
        if (imageParameters.getyMinRight() > Double.NEGATIVE_INFINITY) {
            yBottomR = imageParameters.getyMinRight();
        }

        ySpanL = yTopL - yBottomL;
        ySpanR = yTopR - yBottomR;

        if (ySpanL == 0) {
            yTopL++;
            ySpanL++;
        }
        if (ySpanR == 0) {
            yTopR++;
            ySpanR++;
        }

        graphHeight = yMax - yMin;
        yScaleFactorL = graphHeight / ySpanL;
        yScaleFactorR = graphHeight / ySpanR;

        yLabelValuesL = getYLabelValues(yBottomL, yTopL, yStepL);
        yLabelValuesR = getYLabelValues(yBottomR, yTopR, yStepR);

        FontMetrics fontMetrics = g2d.getFontMetrics(imageParameters.getFont());

        yLabelsL = new ArrayList<>();
        yLabelsR = new ArrayList<>();
        for (Double value : yLabelValuesL) {
            String label = makeLabel(value, yStepL, ySpanL);
            yLabelsL.add(label);
            if (fontMetrics.stringWidth(label) > yLabelWidthL) yLabelWidthL = fontMetrics.stringWidth(label);
        }
        for (Double value : yLabelValuesR) {
            String label = makeLabel(value, yStepR, ySpanR);
            yLabelsR.add(label);
            if (fontMetrics.stringWidth(label) > yLabelWidthR) yLabelWidthR = fontMetrics.stringWidth(label);
        }

        int xxMin = (int) (imageParameters.getMargin() + (yLabelWidthL * 1.15));
        if (xMin < xxMin) {
            xMin = xxMin;
        }

        int xxMax = (int) (imageParameters.getWidth() - (yLabelWidthR * 1.15));
        if (xMax >= xxMax) {
            xMax = xxMax;
        }
    }

    protected void setupYAxis() throws LogarithmicScaleNotAllowed {
        List<DecoratedTimeSeries> seriesWithMissingValues = new ArrayList<>();
        for (DecoratedTimeSeries ts : data) {
            for (Double value : ts.getValues()) {
                if (value == null) {
                    seriesWithMissingValues.add(ts);
                    break;
                }
            }
        }

        double yMinValue = Double.POSITIVE_INFINITY;
        for (DecoratedTimeSeries ts : data) {
            if (!ts.hasOption(TimeSeriesOption.DRAW_AS_INFINITE)) {
                double mm = GraphUtils.safeMin(ts);
                yMinValue = mm < yMinValue ? mm : yMinValue;
            }
        }

        if (yMinValue > 0 && imageParameters.isDrawNullAsZero() && seriesWithMissingValues.size() > 0) {
            yMinValue = 0;
        }


        double yMaxValue;
        if (imageParameters.getAreaMode().equals(ImageParameters.AreaMode.STACKED)) {
            yMaxValue = GraphUtils.maxSum(data);
        } else {
            yMaxValue = GraphUtils.safeMax(data);
        }

        if (yMaxValue < 0 && imageParameters.isDrawNullAsZero() && seriesWithMissingValues.size() > 0) {
            yMaxValue = 0;
        }

        if (yMinValue == Double.POSITIVE_INFINITY) {
            yMinValue = 0;
        }

        if (yMaxValue == Double.NEGATIVE_INFINITY) {
            yMaxValue = 0;
        }

        if (imageParameters.getyMax() != Double.POSITIVE_INFINITY) {
            yMaxValue = imageParameters.getyMax();
        }

        if (imageParameters.getyMin() != Double.NEGATIVE_INFINITY) {
            yMinValue = imageParameters.getyMin();
        }

        if (yMaxValue <= yMinValue) {
            yMaxValue = yMinValue + 1;
        }

        double yVariance = yMaxValue - yMinValue;
        double order;
        double orderFactor;

        order = Math.log10(yVariance);
        orderFactor = Math.pow(10, Math.floor(order));

/*
        if (imageParameters.getyUnitSystem().equals(ImageParameters.UnitSystem.BINARY)) {
            order = Math.log(yVariance) / Math.log(2);
            orderFactor = Math.pow(2, Math.floor(order));
        } else {
            order = Math.log10(yVariance);
            orderFactor = Math.pow(10, Math.floor(order));
        }
*/

        double v = yVariance / orderFactor;

        // todo: verify - it's not a pure translation
        double distance = Double.POSITIVE_INFINITY;
        double prettyValue = PRETTY_VALUES[0];
        for (int i = 0; i < imageParameters.getyDivisors().size(); i++) {
            double q = v / imageParameters.getyDivisors().get(i);
            double p = GraphUtils.closest(q, PRETTY_VALUES);

            if (Math.abs(q - p) < distance) {
                distance = Math.abs(q - p);
                prettyValue = p;

            }
        }

        yStep = prettyValue * orderFactor;

        if (imageParameters.getyStep() < Double.POSITIVE_INFINITY) {
            yStep = imageParameters.getyStep();
        }

        yBottom = yStep * Math.floor(yMinValue / yStep);
        yTop = yStep * Math.ceil(yMaxValue / yStep);

        if (imageParameters.getLogBase() != 0 && yMaxValue > 0) {
            yBottom = Math.pow(imageParameters.getLogBase(), Math.floor(Math.log(yMinValue) / Math.log(imageParameters.getLogBase())));
            yTop = Math.pow(imageParameters.getLogBase(), Math.ceil(Math.log(yMaxValue) / Math.log(imageParameters.getLogBase())));
        } else if (imageParameters.getLogBase() != 0 && yMinValue <= 0) {
            throw new LogarithmicScaleNotAllowed("Logarithmic scale specified with a dataset with a minimum value less than or equal to zero");
        }

        if (imageParameters.getyMax() != Double.POSITIVE_INFINITY) {
            yTop = imageParameters.getyMax();
        }

        if (imageParameters.getyMin() != Double.NEGATIVE_INFINITY) {
            yBottom = imageParameters.getyMin();
        }

        ySpan = yTop - yBottom;

        if (ySpan == 0) {
            yTop++;
            ySpan++;
        }

        graphHeight = yMax - yMin;
        yScaleFactor = graphHeight / ySpan;


        if (!imageParameters.isHideAxes()) {
            yLabelValues = getYLabelValues(yBottom, yTop, yStep);
            yLabels = new ArrayList<>();
            yLabelWidth = 0;
            FontMetrics fontMetrics = g2d.getFontMetrics(imageParameters.getFont());

            for (Double value : yLabelValues) {
                String label = makeLabel(value);
                yLabels.add(label);
                if (fontMetrics.stringWidth(label) > yLabelWidth) yLabelWidth = fontMetrics.stringWidth(label);
            }

            if (!imageParameters.isHideYAxis()) {
                if (imageParameters.getyAxisSide().equals(ImageParameters.Side.LEFT)) {
                    int xxMin = (int) (imageParameters.getMargin() + yLabelWidth * 1.15);
                    if (xMin < xxMin) {
                        xMin = xxMin;
                    }
                } else {
                    int xxMax = (int) (imageParameters.getMargin() - yLabelWidth * 1.15);
                    if (xMax >= xxMax) {
                        xMax = xxMax;
                    }
                }
            }

        } else {
            yLabelValues = new ArrayList<>();
            yLabels = new ArrayList<>();
            yLabelWidth = 0;
        }
    }

    protected void setupXAxis() {
        startDateTime = new DateTime(startTime * 1000, renderParameters.getTz());
        endDateTime = new DateTime(endTime * 1000, renderParameters.getTz());

        double secondsPerPixel = (endTime - startTime) / graphWidth;
        xScaleFactor = (double) graphWidth / (endTime - startTime);

        xAxisConfig = XAxisConfigProvider.getXAxisConfig(secondsPerPixel, endTime - startTime);

        xLabelStep = xAxisConfig.getLabelUnit() * xAxisConfig.getLabelStep();
        xMinorGridStep = (long) (xAxisConfig.getMinorGridUnit() * xAxisConfig.getMinorGridStep());
        xMajorGridStep = xAxisConfig.getMajorGridUnit() * xAxisConfig.getMajorGridStep();

    }

    protected void drawLabels() {
        // Draw the Y-labels
        if (!imageParameters.isHideYAxis()) {
            if (!secondYAxis) {
                for (int i = 0; i < yLabelValues.size(); i++) {
                    int x;
                    if (imageParameters.getyAxisSide().equals(ImageParameters.Side.LEFT)) {
                        x = (int) (xMin - (yLabelWidth * 0.15));
                    } else {
                        x = (int) (xMax + (yLabelWidth * 0.15));
                    }

                    int y = getYCoord(yLabelValues.get(i));
                    if (y < 0) y = 0;

                    if (imageParameters.getyAxisSide().equals(ImageParameters.Side.LEFT)) {
                        drawText(x, y, yLabels.get(i), HorizontalAlign.RIGHT, VerticalAlign.MIDDLE);
                    } else {
                        drawText(x, y, yLabels.get(i), HorizontalAlign.LEFT, VerticalAlign.MIDDLE);
                    }
                }
            } else {
                for (int i = 0; i < yLabelValuesL.size(); i++) {
                    int x = (int) (xMin - (yLabelWidthL * 0.15));
                    int y = getYCoordLeft(yLabelValuesL.get(i));
                    if (y < 0) y = 0;
                    drawText(x, y, yLabelsL.get(i), HorizontalAlign.RIGHT, VerticalAlign.MIDDLE);
                }

                for (int i = 0; i < yLabelValuesR.size(); i++) {
                    int x = (int) (xMax + (yLabelWidthR * 0.15));
                    int y = getYCoordRight(yLabelValuesR.get(i));
                    if (y < 0) y = 0;
                    drawText(x, y, yLabelsR.get(i), HorizontalAlign.LEFT, VerticalAlign.MIDDLE);
                }
            }
        }

        // Draw the X-labels
        long labelDt = 0;
        long labelDelta = 1;
        if (xAxisConfig.getLabelUnit() == XAxisConfigProvider.SEC) {
            labelDt = startTime - (long) (startTime % xAxisConfig.getLabelStep());
            labelDelta = (long) xAxisConfig.getLabelStep();
        } else if (xAxisConfig.getLabelUnit() == XAxisConfigProvider.MIN) {
            DateTime tdt = new DateTime(startTime * 1000, renderParameters.getTz());
            labelDt = tdt.withSecondOfMinute(0).withMinuteOfHour((int) (tdt.getMinuteOfHour() - (tdt.getMinuteOfHour() % xAxisConfig.getLabelStep()))).getMillis() / 1000;
            labelDelta = (long) xAxisConfig.getLabelStep() * 60;
        } else if (xAxisConfig.getLabelUnit() == XAxisConfigProvider.HOUR) {
            DateTime tdt = new DateTime(startTime * 1000, renderParameters.getTz());
            labelDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay((int) (tdt.getHourOfDay() - (tdt.getHourOfDay() % xAxisConfig.getLabelStep()))).getMillis() / 1000;
            labelDelta = (long) xAxisConfig.getLabelStep() * 60 * 60;
        } else if (xAxisConfig.getLabelUnit() == XAxisConfigProvider.DAY) {
            DateTime tdt = new DateTime(startTime * 1000, renderParameters.getTz());
            labelDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(0).getMillis() / 1000;
            labelDelta = (long) xAxisConfig.getLabelStep() * 60 * 60 * 24;
        }

        while (labelDt < startTime) labelDt += labelDelta;

        DateTime ddt = new DateTime(labelDt * 1000, renderParameters.getTz());

        FontMetrics fontMetrics = g2d.getFontMetrics(imageParameters.getFont());

        while (ddt.isBefore(endDateTime)) {
            String label = ddt.toString(DateTimeFormat.forPattern(xAxisConfig.getFormat()));
            int x = (int) (xMin + (Seconds.secondsBetween(startDateTime, ddt).getSeconds() * xScaleFactor));
            int y = yMax + fontMetrics.getMaxAscent();
            drawText(x, y, label, HorizontalAlign.CENTER, VerticalAlign.TOP);

            ddt = ddt.plusSeconds((int) labelDelta);
        }
    }

    protected void drawGridLines() {
        g2d.setStroke(new BasicStroke(0f));

        //Horizontal grid lines
        int leftSide = xMin;
        int rightSide = xMax;
        List<Double> labelValues = secondYAxis ? yLabelValuesL : yLabelValues;

        for (int i = 0; i < labelValues.size(); i++) {
            g2d.setColor(imageParameters.getMajorGridLineColor());

            int y = secondYAxis ? getYCoordLeft(labelValues.get(i)) : getYCoord(labelValues.get(i));
            if (y < 0) continue;

            g2d.drawLine(leftSide, y, rightSide, y);

            // draw minor gridlines if this isn't the last label
            g2d.setColor(imageParameters.getMinorGridLineColor());
            if (imageParameters.getMinorY() >= 1 && i < (labelValues.size() - 1)) {
                double distance = ((labelValues.get(i + 1) - labelValues.get(i)) / (1 + imageParameters.getMinorY()));

                for (int minor = 0; minor < imageParameters.getMinorY(); minor++) {
                    double minorValue = (labelValues.get(i) + ((1 + minor) * distance));

                    int yTopFactor = imageParameters.getLogBase() != 0 ? (int) (imageParameters.getLogBase() * imageParameters.getLogBase()) : 1;

                    if (secondYAxis) {
                        if (minorValue > yTopFactor * yTopL) continue;
                    } else {
                        if (minorValue > yTopFactor * yTop) continue;
                    }

                    int yMinor = secondYAxis ? getYCoordLeft(minorValue) : getYCoord(minorValue);
                    if (yMinor < 0) continue;

                    g2d.drawLine(leftSide, yMinor, rightSide, yMinor);
                }
            }
        }

        // Vertical grid lines
        int top = yMin;
        int bottom = yMax;

        long dt = 0;
        long delta = 1;
        if (xAxisConfig.getMinorGridUnit() == XAxisConfigProvider.SEC) {
            dt = startTime - (long) (startTime % xAxisConfig.getMinorGridStep());
            delta = (long) xAxisConfig.getMinorGridStep();
        } else if (xAxisConfig.getMinorGridUnit() == XAxisConfigProvider.MIN) {
            DateTime tdt = new DateTime(startTime * 1000, renderParameters.getTz());
            dt = tdt.withSecondOfMinute(0).withMinuteOfHour((int) (tdt.getMinuteOfHour() - (tdt.getMinuteOfHour() % xAxisConfig.getMinorGridStep()))).getMillis() / 1000;
            delta = (long) xAxisConfig.getMinorGridStep() * 60;
        } else if (xAxisConfig.getMinorGridUnit() == XAxisConfigProvider.HOUR) {
            DateTime tdt = new DateTime(startTime * 1000, renderParameters.getTz());
            dt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay((int) (tdt.getHourOfDay() - (tdt.getHourOfDay() % xAxisConfig.getMinorGridStep()))).getMillis() / 1000;
            delta = (long) xAxisConfig.getMinorGridStep() * 60 * 60;
        } else if (xAxisConfig.getMinorGridUnit() == XAxisConfigProvider.DAY) {
            DateTime tdt = new DateTime(startTime * 1000, renderParameters.getTz());
            dt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(0).getMillis() / 1000;
            delta = (long) xAxisConfig.getMinorGridStep() * 60 * 60 * 24;
        }

        while (dt < startTime) dt += delta;

        DateTime ddt = new DateTime(dt * 1000, renderParameters.getTz());

        g2d.setColor(imageParameters.getMinorGridLineColor());

        while (ddt.isBefore(endDateTime)) {
            int x = (int) (xMin + (Seconds.secondsBetween(startDateTime, ddt).getSeconds() * xScaleFactor));

            if (x < xMax) {
                g2d.drawLine(x, bottom, x, top);
            }

            ddt = ddt.plusSeconds((int) delta);
        }

        // Now we do the major grid lines
        g2d.setColor(imageParameters.getMajorGridLineColor());
        long majorDt = 0;
        long majorDelta = 1;

        if (xAxisConfig.getMajorGridUnit() == XAxisConfigProvider.SEC) {
            majorDt = startTime - (long) (startTime % xAxisConfig.getMajorGridStep());
            majorDelta = (long) xAxisConfig.getMajorGridStep();
        } else if (xAxisConfig.getMajorGridUnit() == XAxisConfigProvider.MIN) {
            DateTime tdt = new DateTime(startTime * 1000, renderParameters.getTz());
            majorDt = tdt.withSecondOfMinute(0).withMinuteOfHour((int) (tdt.getMinuteOfHour() - (tdt.getMinuteOfHour() % xAxisConfig.getMajorGridStep()))).getMillis() / 1000;
            majorDelta = (long) xAxisConfig.getMajorGridStep() * 60;
        } else if (xAxisConfig.getMajorGridUnit() == XAxisConfigProvider.HOUR) {
            DateTime tdt = new DateTime(startTime * 1000, renderParameters.getTz());
            majorDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay((int) (tdt.getHourOfDay() - (tdt.getHourOfDay() % xAxisConfig.getMajorGridStep()))).getMillis() / 1000;
            majorDelta = (long) xAxisConfig.getMajorGridStep() * 60 * 60;
        } else if (xAxisConfig.getMajorGridUnit() == XAxisConfigProvider.DAY) {
            DateTime tdt = new DateTime(startTime * 1000, renderParameters.getTz());
            majorDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(0).getMillis() / 1000;
            majorDelta = (long) xAxisConfig.getMajorGridStep() * 60 * 60 * 24;
        }

        while (majorDt < startTime) majorDt += majorDelta;

        ddt = new DateTime(majorDt * 1000, renderParameters.getTz());

        while (ddt.isBefore(endDateTime)) {
            int x = (int) (xMin + (Seconds.secondsBetween(startDateTime, ddt).getSeconds() * xScaleFactor));

            if (x < xMax) {
                g2d.drawLine(x, bottom, x, top);
            }

            ddt = ddt.plusSeconds((int) majorDelta);
        }

        //Draw side borders for our graph area
        g2d.drawLine(xMax, bottom, xMax, top);
        g2d.drawLine(xMin, bottom, xMin, top);
    }

    protected void drawLines() {
        int width = (int) imageParameters.getLineWidth();
        g2d.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        int originalWidth = width;

        boolean singleStacked = false;

        for (DecoratedTimeSeries ts : data) {
            if (ts.hasOption(TimeSeriesOption.STACKED)) {
                singleStacked = true;
                break;
            }
        }

        //todo: optimize
        if (singleStacked) {
            sortStacked();
        }

        if (imageParameters.getAreaMode().equals(ImageParameters.AreaMode.STACKED) && !secondYAxis) {
            double[] total = new double[data.get(0).getValues().length];

            for (DecoratedTimeSeries ts : data) {
                if (ts.hasOption(TimeSeriesOption.DRAW_AS_INFINITE)) continue;

                ts.setOption(TimeSeriesOption.STACKED, true);
                for (int i = 0; i < ts.getValues().length; i++) {
                    if (ts.getValues()[i] != null) {
                        double original = ts.getValues()[i];
                        ts.getValues()[i] += total[i];
                        total[i] += original;
                    }
                }
            }
        } else if (imageParameters.getAreaMode().equals(ImageParameters.AreaMode.FIRST)) {
            data.get(0).setOption(TimeSeriesOption.STACKED, true);
        } else if (imageParameters.getAreaMode().equals(ImageParameters.AreaMode.ALL)) {
            for (DecoratedTimeSeries ts : data) {
                if (!ts.hasOption(TimeSeriesOption.DRAW_AS_INFINITE)) {
                    ts.setOption(TimeSeriesOption.STACKED, true);
                }
            }
        }

        //todo: implement this stuff. I'm really not happy about creating new series, putting strokes there, etc
/*
    # apply alpha channel and create separate stroke series
    if self.params.get('areaAlpha'):
      try:
        alpha = float(self.params['areaAlpha'])
      except ValueError:
        alpha = 0.5
        pass

      strokeSeries = []
      for series in self.data:
        if 'stacked' in series.options:
          series.options['alpha'] = alpha

          newSeries = TimeSeries(series.name, series.start, series.end, series.step*series.valuesPerPoint, [x for x in series])
          newSeries.xStep = series.xStep
          newSeries.color = series.color
          if 'secondYAxis' in series.options:
            newSeries.options['secondYAxis'] = True
          strokeSeries.append(newSeries)
      self.data += strokeSeries

    # setup the clip region
    self.ctx.set_line_width(1.0)
    self.ctx.rectangle(self.area['xmin'], self.area['ymin'], self.area['xmax'] - self.area['xmin'], self.area['ymax'] - self.area['ymin'])
    self.ctx.clip()
    self.ctx.set_line_width(originalWidth)

    # save clip to restore once stacked areas are drawn
    self.ctx.save()
    clipRestored = False
         */

        for (DecoratedTimeSeries ts : data) {
            //todo: implement this stuff
            /*
              if 'stacked' not in series.options:
                # stacked areas are always drawn first. if this series is not stacked, we finished stacking.
                # reset the clip region so lines can show up on top of the stacked areas.
                if not clipRestored:
                  clipRestored = True
                  self.ctx.restore()
             */

            if (ts.hasOption(TimeSeriesOption.LINE_WIDTH)) {
                g2d.setStroke(new BasicStroke((Float) ts.getOption(TimeSeriesOption.LINE_WIDTH)));
            }

/*
            if 'dashed' in series.options:
                self.ctx.set_dash([ series.options['dashed'] ], 1)
            else:
                self.ctx.set_dash([], 0)
*/

            //todo: most probably we don't need this stuff - we are aligned by construction
/*
          # Shift the beginning of drawing area to the start of the series if the
          # graph itself has a larger range
          missingPoints = (series.start - self.startTime) / series.step
          startShift = series.xStep * (missingPoints / series.valuesPerPoint)
          x = float(self.area['xmin']) + startShift + (self.lineWidth / 2.0)
          y = float(self.area['ymin'])

          startX = x
 */
            //todo: is there a better way to apply alpha?
            if (ts.hasOption(TimeSeriesOption.INVISIBLE)) {
                g2d.setColor(new Color(0, 0, 0, 0));
            } else {
                Color c = (Color) ts.getOption(TimeSeriesOption.COLOR);
                g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), ts.hasOption(TimeSeriesOption.ALPHA) ? (int) ((Float) ts.getOption(TimeSeriesOption.ALPHA) * 255) : 255));
            }

            double x = xMin;
            double previousX = xMin;
            int y = -1;
            int previousY =  -1;
            Double[] values = ts.getConsolidatedValues();
            int consecutiveNulls = 0;


            for (int index = 0; index < values.length; index++) {
                double value;

                if (values[index] == null) {
                    if (imageParameters.isDrawNullAsZero()) {
                        value = 0;
                    } else {
                        x += ts.getxStep();
                        consecutiveNulls++;
                        continue;
                    }
                } else {
                    value = values[index];
                }

                if (secondYAxis) {
                    if (ts.hasOption(TimeSeriesOption.SECOND_Y_AXIS)) {
                        y = getYCoordRight(value);
                    } else {
                        y = getYCoordLeft(value);
                    }
                } else {
                    y = getYCoord(value);
                }

                y = y < 0 ? 0 : y;

                if (ts.hasOption(TimeSeriesOption.DRAW_AS_INFINITE) && value > 0) {
                    g2d.drawLine((int) x, yMax, (int) x, yMin);
                    x += ts.getxStep();
                    continue;
                }

                // todo: implement this
                /*
                  if consecutiveNones > 0:
                    startX = x

                  if self.lineMode == 'staircase':
                    if consecutiveNones > 0:
                      self.ctx.move_to(x, y)
                    else:
                      self.ctx.line_to(x, y)

                    x += series.xStep
                    self.ctx.line_to(x, y)

                  elif self.lineMode == 'slope':
                    if consecutiveNones > 0:
                      self.ctx.move_to(x, y)

                    self.ctx.line_to(x, y)
                    x += series.xStep

                  elif self.lineMode == 'connected':
                    # If if the gap is larger than the connectedLimit or if this is the
                    # first non-None datapoint in the series, start drawing from that datapoint.
                    if consecutiveNones > self.connectedLimit or consecutiveNones == index:
                       self.ctx.move_to(x, y)

                    self.ctx.line_to(x, y)
                    x += series.xStep

                 */

                previousX = previousX < 0 ? x : previousX;
                previousY = previousY < 0 ? y : previousY;

                if (imageParameters.getLineMode().equals(ImageParameters.LineMode.SLOPE)) {
                    if (consecutiveNulls > 0) {
                        previousX = x;
                        previousY = y;
                    }

                    g2d.drawLine((int) previousX, previousY, (int) x, y);
                }

                // todo: implement this??
                /*
        if value is None:
          if consecutiveNones == 0:
            self.ctx.line_to(x, y)
            if 'stacked' in series.options: #Close off and fill area before unknown interval
              if self.secondYAxis:
                if 'secondYAxis' in series.options:
                  self.fillAreaAndClip(x, y, startX, self.getYCoord(0, "right"))
                else:
                  self.fillAreaAndClip(x, y, startX, self.getYCoord(0, "left"))
              else:
                self.fillAreaAndClip(x, y, startX, self.getYCoord(0))

          x += series.xStep
          consecutiveNones += 1


                                 */

                consecutiveNulls = 0;
                previousX = x;
                previousY = y;

                x += ts.getxStep();
            }

            //todo: check if this is needed
            /*
      if 'stacked' in series.options:
        if self.lineMode == 'staircase':
          xPos = x
        else:
          xPos = x-series.xStep
        if self.secondYAxis:
          if 'secondYAxis' in series.options:
            areaYFrom = self.getYCoord(0, "right")
          else:
            areaYFrom = self.getYCoord(0, "left")
        else:
          areaYFrom = self.getYCoord(0)

        self.fillAreaAndClip(xPos, y, startX, areaYFrom)

      else:
        self.ctx.stroke()

      self.ctx.set_line_width(originalWidth) # return to the original line width
      if 'dash' in series.options: # if we changed the dash setting before, change it back now
        if dash:
          self.ctx.set_dash(dash,1)
        else:
          self.ctx.set_dash([],0)
                         */

        }
    }

    private void sortStacked() {
        List<DecoratedTimeSeries> newData = new ArrayList<>();

        for (DecoratedTimeSeries ts : data) {
            if (ts.hasOption(TimeSeriesOption.STACKED)) {
                newData.add(ts);
            }
        }
        for (DecoratedTimeSeries ts : data) {
            if (!ts.hasOption(TimeSeriesOption.STACKED)) {
                newData.add(ts);
            }
        }

        data = newData;
    }

    private int getYCoordRight(double value) {
        double highestValue = Collections.max(yLabelValuesR);
        double lowestValue = Collections.min(yLabelValuesR);
        int pixelRange = yMax - yMin;

        double relativeValue = value - lowestValue;
        double valueRange = highestValue - lowestValue;

        if (imageParameters.getLogBase() != 0) {
            if (value < 0) {
                return -1;
            }

            relativeValue = Math.log(value) / Math.log(imageParameters.getLogBase()) - Math.log(lowestValue) / Math.log(imageParameters.getLogBase());
            valueRange = Math.log(highestValue) / Math.log(imageParameters.getLogBase()) - Math.log(lowestValue) / Math.log(imageParameters.getLogBase());
        }

        double pixelToValueRatio = pixelRange / valueRange;
        double valueInPixels = pixelToValueRatio * relativeValue;
        return (int) (yMax - valueInPixels);
    }


    private int getYCoordLeft(double value) {
        double highestValue = Collections.max(yLabelValuesL);
        double lowestValue = Collections.min(yLabelValuesL);
        int pixelRange = yMax - yMin;

        double relativeValue = value - lowestValue;
        double valueRange = highestValue - lowestValue;

        if (imageParameters.getLogBase() != 0) {
            if (value < 0) {
                return -1;
            }

            relativeValue = Math.log(value) / Math.log(imageParameters.getLogBase()) - Math.log(lowestValue) / Math.log(imageParameters.getLogBase());
            valueRange = Math.log(highestValue) / Math.log(imageParameters.getLogBase()) - Math.log(lowestValue) / Math.log(imageParameters.getLogBase());
        }

        double pixelToValueRatio = pixelRange / valueRange;
        double valueInPixels = pixelToValueRatio * relativeValue;
        return (int) (yMax - valueInPixels);
    }

    private int getYCoord(double value) {
        double highestValue = Collections.max(yLabelValues);
        double lowestValue = Collections.min(yLabelValues);
        int pixelRange = yMax - yMin;

        double relativeValue = value - lowestValue;
        double valueRange = highestValue - lowestValue;

        if (imageParameters.getLogBase() != 0) {
            if (value < 0) {
                return -1;
            }

            relativeValue = Math.log(value) / Math.log(imageParameters.getLogBase()) - Math.log(lowestValue) / Math.log(imageParameters.getLogBase());
            valueRange = Math.log(highestValue) / Math.log(imageParameters.getLogBase()) - Math.log(lowestValue) / Math.log(imageParameters.getLogBase());
        }

        double pixelToValueRatio = pixelRange / valueRange;
        double valueInPixels = pixelToValueRatio * relativeValue;
        return (int) (yMax - valueInPixels);
    }

    private List<Double> getYLabelValues(double min, double max, double step) {
        if (imageParameters.getLogBase() != 0) {
            return logRange(imageParameters.getLogBase(), min, max);
        } else {
            return fRange(step, min, max);
        }

    }

    protected String makeLabel(double value, double step, double span) {
        double tmpValue = formatUnitValue(value, step, imageParameters.getyUnitSystem());
        String prefix = formatUnitPrefix(value, step, imageParameters.getyUnitSystem());

        double ySpan = formatUnitValue(span, step, imageParameters.getyUnitSystem());
        String spanPrefix = formatUnitPrefix(span, step, imageParameters.getyUnitSystem());

        value = tmpValue;

        if (value < 0.1) {
            return value + prefix;
        } else if (value < 1.0) {
            return String.format("%.2f %s", value, prefix);
        }

        if (ySpan > 10 || !spanPrefix.equals(prefix)) {
            return String.format("%.1f %s", value, prefix);
/*
            if (value != Math.floor(value)) {
                return String.format("%.1f %s", value, prefix);
            } else {
                return String.format("%d %s", (int) value, prefix);
            }
*/
        } else if (ySpan > 3) {
            return String.format("%.1f %s", value, prefix);
        } else if (ySpan > 0.1) {
            return String.format("%.2f %s", value, prefix);
        } else {
            return value + prefix;
        }
    }

    protected String makeLabel(double value) {
        return makeLabel(value, yStep, ySpan);
    }

    private String formatUnitPrefix(double value, double step, ImageParameters.UnitSystem system) {
        for (Map.Entry<String, Double> entry : imageParameters.getyUnitSystem().getPrefixMap().entrySet()) {
            if (Math.abs(value) >= entry.getValue() && step >= entry.getValue()) {
                return entry.getKey();
            }

        }

        return "";
    }

    private double formatUnitValue(double value, double step, ImageParameters.UnitSystem system) {
        for (Map.Entry<String, Double> entry : imageParameters.getyUnitSystem().getPrefixMap().entrySet()) {
            if (Math.abs(value) >= entry.getValue() && step >= entry.getValue()) {
                double v2 = value / entry.getValue();
                if (v2 - Math.floor(v2) < 0.00000000001 && value > 1) {
                    v2 = Math.floor(v2);
                }
                return v2;
            }
        }

        if (value - Math.floor(value) < 0.00000000001 && value > 1) {
            return Math.floor(value);
        }

        return value;
    }

    private List<Double> logRange(double base, double min, double max) {
        List<Double> result = new ArrayList<>();
        double current = min;

        if (min > 0) {
            current = Math.floor(Math.log(min) / Math.log(base));
        }

        double factor = current;

        while (current < max) {
            current = Math.pow(base, factor);
            result.add(current);
            factor++;
        }

        return result;
    }

    private List<Double> fRange(double step, double min, double max) {
        List<Double> result = new ArrayList<>();
        double f = min;

        while (f <= max) {
            result.add(f);
            f += step;
            if (f == min) {
                result.add(max);
                break;
            }
        }
        return result;
    }

    protected enum HorizontalAlign {
        LEFT, CENTER, RIGHT;
    }

    protected enum VerticalAlign {
        TOP, MIDDLE, BOTTOM, BASELINE;
    }
}
