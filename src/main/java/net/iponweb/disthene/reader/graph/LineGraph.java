package net.iponweb.disthene.reader.graph;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.beans.TimeSeriesOption;
import net.iponweb.disthene.reader.handler.parameters.ImageParameters;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class LineGraph extends Graph {

    private double lineWidth = 1.2;



    public LineGraph(ImageParameters imageParameters, List<TimeSeries> data) {
        super(imageParameters, data);
    }

    @Override
    public byte[] drawGraph() {
        if (data.size() == 0) {
            return drawNoData();
        }

        long startTime = Long.MAX_VALUE;
        long endTime = Long.MIN_VALUE;

        for(TimeSeries ts : data) {
            startTime = Math.min(startTime, ts.getFrom());
            endTime = Math.max(endTime, ts.getTo());

            if (ts.hasOption(TimeSeriesOption.SECOND_Y_AXIS)) {
                secondYAxis = true;
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
            for (TimeSeries ts : data) {
                if (ts.getValues().length <= 1) {
                    imageParameters.setLineMode(ImageParameters.LineMode.STAIRCASE);
                }
            }
        }

        //assign colors
        int i = 0;
        for(TimeSeries ts : data) {
            if (!ts.hasOption(TimeSeriesOption.COLOR)) {
                ts.setOption(TimeSeriesOption.COLOR, imageParameters.getColorList().get(i % imageParameters.getColorList().size()));
                i++;
            }
        }

        //todo: remove
        imageParameters.setTitle("Asasasas");


        if (!imageParameters.getTitle().isEmpty()) {
            drawTitle();
        }

        //todo:
/*
        if (!imageParameters.getVerticalTitle().isEmpty()) {
            drawVerticalTitle();
        }

    if self.secondYAxis and params.get('vtitleRight'):
      self.drawVTitle( str(params['vtitleRight']), rightAlign=True )
    self.setFont()
*/

        //todo: config legend max items
        if (!imageParameters.isHideLegend() && (data.size() <= 10)) {
            List<String> legends = new ArrayList<>();
            List<Color> colors = new ArrayList<>();
            List<Boolean> secondYAxes = new ArrayList<>();

            for(TimeSeries ts : data) {
                legends.add(ts.getName());
                colors.add((Color) ts.getOption(TimeSeriesOption.COLOR));
                secondYAxes.add(ts.hasOption(TimeSeriesOption.SECOND_Y_AXIS));
            }

            drawLegend(legends, colors, secondYAxes, imageParameters.isUniqueLegend());
        }

        return getBytes();
    }

    private byte[] drawNoData() {
        int x = imageParameters.getWidth() / 2;
        int y = imageParameters.getHeight() / 2;

        g2d.setPaint(Color.RED);
        Font font = new Font(imageParameters.getFont().getName(), imageParameters.getFont().getStyle(),
                (int) Math.log(imageParameters.getHeight() * imageParameters.getWidth()));

        drawText(x, y, "No Data", font, Color.RED, HorizontalAlign.CENTER, VerticalAlign.TOP);

        return getBytes();
    }

}
