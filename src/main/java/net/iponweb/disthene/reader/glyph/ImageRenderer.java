package net.iponweb.disthene.reader.glyph;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.handler.parameters.ImageParameters;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class ImageRenderer {
    final static Logger logger = Logger.getLogger(ImageRenderer.class);

    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color MAJOR_LINE_COLOR = new Color(114, 114, 114);
    private static final Color MINOR_LINE_COLOR = new Color(44, 44, 44);
    private static final Color LABEL_COLOR = new Color(255, 255, 255);
    private static final int MARGIN = 10;


    private ImageParameters imageParameters;
    private List<TimeSeries> timeSeriesList;

    private BufferedImage image;
    private Graphics2D g2d;

    private long startTime;
    private long endTime;


    public ImageRenderer(ImageParameters imageParameters, List<TimeSeries> timeSeriesList) {
        this.imageParameters = imageParameters;
        this.timeSeriesList = timeSeriesList;
    }

    //todo: image format
    public byte[] render() {
        image = new BufferedImage(imageParameters.getWidth(), imageParameters.getHeight(), BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        g2d.setPaint(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, imageParameters.getWidth(), imageParameters.getHeight());

        if (timeSeriesList.size() > 0) {
            startTime = timeSeriesList.get(0).getFrom();
            endTime = timeSeriesList.get(0).getTo();
            drawGrid();

        }



        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error(e);
            return new byte[0];
        }

    }

    private void drawGrid() {
        // draw minor vertical grid lines
        float secondsPerPixel = (endTime - startTime) / (imageParameters.getWidth() - getLeftMargin() - getRightMargin());
        long timeRange = endTime - startTime;
        XAxisConfig config = XAxisConfigProvider.getXAxisConfig(secondsPerPixel, timeRange);
        System.out.println(config);

        double xScaleFactor = (double) imageParameters.getWidth() / (double) timeRange;

        long dt = 0;
        long delta = 1;
        long majorDt = 0;
        long majorDelta = 1;
        long labelDt = 0;
        long labelDelta = 1;
        if (config.getMinorGridUnit() == XAxisConfigProvider.SEC) {
            dt = startTime - (long)(startTime % config.getMinorGridStep());
            delta = (long) config.getMinorGridStep();
        } else if (config.getMinorGridUnit() == XAxisConfigProvider.MIN) {
            DateTime tdt = new DateTime(startTime * 1000);
            dt = tdt.withSecondOfMinute(0).withMinuteOfHour((int) (tdt.getMinuteOfHour() - (tdt.getMinuteOfHour() % config.getMinorGridStep()))).getMillis() / 1000;
            delta = (long) config.getMinorGridStep() * 60;
        } else if (config.getMinorGridUnit() == XAxisConfigProvider.HOUR) {
            DateTime tdt = new DateTime(startTime * 1000);
            dt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay((int) (tdt.getHourOfDay() - (tdt.getHourOfDay() % config.getMinorGridStep()))).getMillis() / 1000;
            delta = (long) config.getMinorGridStep() * 60 * 60;
        } else if (config.getMinorGridUnit() == XAxisConfigProvider.DAY) {
            DateTime tdt = new DateTime(startTime * 1000);
            dt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(0).getMillis() / 1000;
            delta = (long) config.getMinorGridStep() * 60 * 60 * 24;
        }

        if (config.getMajorGridUnit() == XAxisConfigProvider.SEC) {
            majorDt = startTime - (long)(startTime % config.getMajorGridStep());
            majorDelta = (long) config.getMajorGridStep();
        } else if (config.getMajorGridUnit() == XAxisConfigProvider.MIN) {
            DateTime tdt = new DateTime(startTime * 1000);
            majorDt = tdt.withSecondOfMinute(0).withMinuteOfHour((int) (tdt.getMinuteOfHour() - (tdt.getMinuteOfHour() % config.getMajorGridStep()))).getMillis() / 1000;
            majorDelta = (long) config.getMajorGridStep() * 60;
        } else if (config.getMajorGridUnit() == XAxisConfigProvider.HOUR) {
            DateTime tdt = new DateTime(startTime * 1000);
            majorDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay((int) (tdt.getHourOfDay() - (tdt.getHourOfDay() % config.getMajorGridStep()))).getMillis() / 1000;
            majorDelta = (long) config.getMajorGridStep() * 60 * 60;
        } else if (config.getMajorGridUnit() == XAxisConfigProvider.DAY) {
            DateTime tdt = new DateTime(startTime * 1000);
            majorDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(0).getMillis() / 1000;
            majorDelta = (long) config.getMajorGridStep() * 60 * 60 * 24;
        }

        if (config.getLabelUnit() == XAxisConfigProvider.SEC) {
            labelDt = startTime - (long)(startTime % config.getLabelStep());
            labelDelta = (long) config.getLabelStep();
        } else if (config.getLabelUnit() == XAxisConfigProvider.MIN) {
            DateTime tdt = new DateTime(startTime * 1000);
            labelDt = tdt.withSecondOfMinute(0).withMinuteOfHour((int) (tdt.getMinuteOfHour() - (tdt.getMinuteOfHour() % config.getLabelStep()))).getMillis() / 1000;
            labelDelta = (long) config.getLabelStep() * 60;
        } else if (config.getLabelUnit() == XAxisConfigProvider.HOUR) {
            DateTime tdt = new DateTime(startTime * 1000);
            labelDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay((int) (tdt.getHourOfDay() - (tdt.getHourOfDay() % config.getLabelStep()))).getMillis() / 1000;
            labelDelta = (long) config.getLabelStep() * 60 * 60;
        } else if (config.getLabelUnit() == XAxisConfigProvider.DAY) {
            DateTime tdt = new DateTime(startTime * 1000);
            labelDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(0).getMillis() / 1000;
            labelDelta = (long) config.getLabelStep() * 60 * 60 * 24;
        }

        while (dt < startTime) dt += delta;
        while (majorDt < startTime) majorDt += majorDelta;
        while (labelDt < startTime) labelDt += labelDelta;

        g2d.setPaint(MINOR_LINE_COLOR);
        g2d.setStroke(new BasicStroke(0f));

        while (dt < endTime) {
            int x = (int) (getLeftMargin() + (dt - startTime) * xScaleFactor);

            if (x < (imageParameters.getWidth() - getRightMargin())) {
                g2d.drawLine(x, getTopMargin(), x, imageParameters.getHeight() - getBottomMargin());
            }

            dt += delta;
        }

        g2d.setPaint(MAJOR_LINE_COLOR);
        g2d.setStroke(new BasicStroke(1f));

        while (majorDt < endTime) {
            int x = (int) (getLeftMargin() + (majorDt - startTime) * xScaleFactor);

            if (x < (imageParameters.getWidth() - getRightMargin())) {
                g2d.drawLine(x, getTopMargin(), x, imageParameters.getHeight() - getBottomMargin());
            }

            majorDt += majorDelta;
        }

        g2d.drawLine(getLeftMargin(), getTopMargin(), getLeftMargin(), imageParameters.getHeight() - getBottomMargin());
        g2d.drawLine(getLeftMargin(), imageParameters.getHeight() - getBottomMargin(), imageParameters.getWidth() - getRightMargin(), imageParameters.getHeight() - getBottomMargin());
        g2d.drawLine(imageParameters.getWidth() - getRightMargin(), getTopMargin(), imageParameters.getWidth() - getRightMargin(), imageParameters.getHeight() - getBottomMargin());
        g2d.drawLine(getLeftMargin(), getTopMargin(), imageParameters.getWidth() - getRightMargin(), getTopMargin());

        g2d.setPaint(LABEL_COLOR);
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics(font);
        int fontHeight = fontMetrics.getHeight();

        while (labelDt < endTime) {
            String label =  (new DateTime(labelDt * 1000, DateTimeZone.UTC)).toString(DateTimeFormat.forPattern(config.getFormat()));
            int labelWidth = fontMetrics.stringWidth(label);
            int x = (int) (getLeftMargin() + (labelDt - startTime) * xScaleFactor - labelWidth / 2);
            int y = imageParameters.getHeight() - getBottomMargin() + fontHeight;

            if (x < (imageParameters.getWidth() - getLeftMargin())) {
                g2d.drawString(label, x, y);
            }

            labelDt += labelDelta;
        }

    }

    private int getLeftMargin() {
        return MARGIN;
    }

    private int getRightMargin() {
        return MARGIN;
    }

    private int getBottomMargin() {
        return MARGIN + getFontHeight();
    }

    private int getTopMargin() {
        return MARGIN;
    }

    private Font getFont() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    }

    private int getFontHeight() {
        return g2d.getFontMetrics(getFont()).getHeight();
    }

}
