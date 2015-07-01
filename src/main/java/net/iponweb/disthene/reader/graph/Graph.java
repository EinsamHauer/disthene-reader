package net.iponweb.disthene.reader.graph;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.handler.parameters.ImageParameters;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrei Ivanov
 *
 * This class and those below in hierarchy are pure translations from graphite-web Python code.
 * This will probably changed some day. But for now reverse engineering the logic is too comaplicated.
 *
 */
public abstract class Graph {
    final static Logger logger = Logger.getLogger(Graph.class);

    protected ImageParameters imageParameters;
    protected List<TimeSeries> data;
    protected List<TimeSeries> dataLeft;
    protected List<TimeSeries> dataRight;
    protected boolean secondYAxis = false;

    protected int xMin;
    protected int xMax;
    protected int yMin;
    protected int yMax;

    protected BufferedImage image;
    protected Graphics2D g2d;


    public Graph(ImageParameters imageParameters, List<TimeSeries> data) {
        this.imageParameters = imageParameters;
        this.data = data;

        xMin = imageParameters.getMargin() + 10;
        xMax = imageParameters.getWidth() - imageParameters.getMargin();
        yMin = imageParameters.getMargin();
        yMax = imageParameters.getHeight() - imageParameters.getMargin();


        image = new BufferedImage(imageParameters.getWidth(), imageParameters.getHeight(), BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        g2d.setPaint(imageParameters.getBackgroundColor());
        g2d.fillRect(0, 0, imageParameters.getWidth(), imageParameters.getHeight());
    }

    public abstract byte[] drawGraph();

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
            case RIGHT: horizontal = textWidth; break;
            case CENTER: horizontal = textWidth / 2;  break;
            default: horizontal = 0; break;
        }

        switch (verticalAlign) {
            case MIDDLE: vertical = fontMetrics.getHeight() / 2 - fontMetrics.getDescent(); break;
            case BOTTOM: vertical = - fontMetrics.getDescent(); break;
            case BASELINE: vertical = 0; break;
            default: vertical = fontMetrics.getAscent();
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

        for(String line : split) {
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
            for(int i = 0; i < legends.size(); i++) {
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

        } else {
            int boxSize = fontMetrics.getHeight() - 1;
            int lineHeight = fontMetrics.getHeight() + 1;
            int labelWidth = fontMetrics.stringWidth(longestLegend) + 2 * (boxSize + padding);
            int columns = (int) Math.floor(imageParameters.getWidth() / labelWidth);
            if (columns < 1) {
                columns = 1;
            }
            int numberOfLines = (int) Math.ceil((double) legends.size() / columns );
            int legendHeight = numberOfLines * (lineHeight + padding);
            yMax -= legendHeight;

            g2d.setStroke(new BasicStroke(1f));

            int x = xMin;
            int y = yMax + (2 * padding);
            for(int i = 0; i < legends.size(); i++) {
                if (secondYAxes.get(i)) {

                } else {
                    g2d.setPaint(colors.get(i));
                    g2d.fillRect(x, y, boxSize, boxSize);
                    // todo; use color dictionary
                    g2d.setPaint(new Color(175, 175, 175));
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



    protected enum HorizontalAlign {
        LEFT, CENTER, RIGHT;
    }

    protected enum VerticalAlign {
        TOP, MIDDLE, BOTTOM, BASELINE;
    }
}
