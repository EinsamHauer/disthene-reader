package net.iponweb.disthene.reader.graph;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.LogarithmicScaleNotAllowed;
import net.iponweb.disthene.reader.handler.parameters.RenderParameters;
import net.iponweb.disthene.reader.utils.CollectionUtils;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class PieGraph extends Graph {

    private double x0;
    private double y0;
    private double radius;

    public PieGraph(RenderParameters renderParameters, List<TimeSeries> data) {
        super(renderParameters, data);
    }

    @Override
    public byte[] drawGraph() throws LogarithmicScaleNotAllowed {
        // aggregate data
        List<Slice> slices = new ArrayList<>();
        double total = 0.;
        for (int i = 0; i < data.size(); i++) {
            Double value;
            if (imageParameters.getPieMode().equals(PieMode.MAXIMUM)) {
                value = CollectionUtils.max(Arrays.asList(data.get(i).getValues()));
            } else if (imageParameters.getPieMode().equals(PieMode.MINIMUM)) {
                value = CollectionUtils.min(Arrays.asList(data.get(i).getValues()));
            } else {
                value = CollectionUtils.average(Arrays.asList(data.get(i).getValues()));
            }
            slices.add(new Slice(data.get(i).getName(), value, imageParameters.getColorList().get(i % imageParameters.getColorList().size())));
            if (value != null) {
                total += value;
            }
        }

        for (Slice slice : slices) {
            slice.setPercent(slice.getValue() != null ? slice.getValue() / total : null);
        }

        if (!imageParameters.getTitle().isEmpty()) {
            drawTitle();
        }

        List<String> legends = new ArrayList<>();
        List<Color> colors = new ArrayList<>();
        List<Boolean> secondYAxes = new ArrayList<>();

        for (Slice slice : slices) {
            legends.add(slice.getName());
            colors.add(slice.getColor());
            secondYAxes.add(false);
        }

        drawLegend(legends, colors, secondYAxes, imageParameters.isUniqueLegend());

        drawSlices(slices);
        drawLabels(slices);

        return getBytes();
    }

    private void drawSlices(List<Slice> slices) {
        double theta = 90;
        double halfX = (xMax - xMin) / 2.0;
        double halfY = (yMax - yMin) / 2.0;
        x0 = xMin + halfX;
        y0 = yMin + halfY;
        radius = Math.min(halfX, halfY) * 0.95;

        for (Slice slice : slices) {
            if (slice.getPercent() == null) continue;
            double phi = theta - 360 * slice.getPercent();
            Arc2D pie = new Arc2D.Double();
            pie.setArcByCenter(x0, y0, radius, theta, phi - theta, Arc2D.PIE);
            g2d.setPaint(slice.getColor());
            g2d.fill(pie);

            slice.setMidAngle((90-theta) + (theta - phi) / 2);
            theta = phi;
        }
    }

    private void drawLabels(List<Slice> slices) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (Slice slice : slices) {
            String label = null;
            if (imageParameters.getPieLabelsStyle().equals(PieLabelsStyle.PERCENT)) {
                if (slice.getPercent() == null || (slice.getPercent() * 100.0 < imageParameters.getPieLabelsMin())) continue;
                label = String.format("%%%.2f", slice.getPercent() * 100.0);
            } else if (imageParameters.getPieLabelsStyle().equals(PieLabelsStyle.NUMBER)) {
                if (slice.getValue() == null) continue;
                if (slice.getValue() < 10 && slice.getValue() != slice.getValue().intValue()) {
                    label = String.format("%.2f", slice.getValue());
                } else {
                    label = String.valueOf(slice.getValue().intValue());
                }
            }

            if (label == null) continue;

            double theta = slice.getMidAngle();
            double x = x0 + (radius / 2) * Math.sin(Math.toRadians(theta));
            double y = y0 - (radius / 2) * Math.cos(Math.toRadians(theta));

            if (imageParameters.getPieLabelsOrientation().equals(PieLabelsOrientation.ROTATED)) {
                drawText((int) x, (int) y, label, imageParameters.getFont(), ColorTable.BLACK, HorizontalAlign.CENTER, VerticalAlign.MIDDLE, theta > 180 ? 90 + theta : theta - 90);
            } else {
                drawText((int) x, (int) y, label, imageParameters.getFont(), ColorTable.BLACK, HorizontalAlign.CENTER, VerticalAlign.MIDDLE);
            }

        }

    }

    private class Slice {
        private String name;
        private Double value;
        private Double percent;
        private Color color;
        private double midAngle;

        public Slice(String name, Double value, Color color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public Double getValue() {
            return value;
        }

        public Color getColor() {
            return color;
        }

        public Double getPercent() {
            return percent;
        }

        public void setPercent(Double percent) {
            this.percent = percent;
        }

        public double getMidAngle() {
            return midAngle;
        }

        public void setMidAngle(double midAngle) {
            this.midAngle = midAngle;
        }
    }
}
