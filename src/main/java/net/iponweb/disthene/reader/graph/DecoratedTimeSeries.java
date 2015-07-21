package net.iponweb.disthene.reader.graph;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.beans.TimeSeriesOption;
import net.iponweb.disthene.reader.utils.CollectionUtils;

import java.util.*;

/**
 * @author Andrei Ivanov
 */
public class DecoratedTimeSeries {

    private TimeSeries timeSeries;
    private double xStep;
    private int valuesPerPoint;


    public DecoratedTimeSeries(TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
    }

    public double getxStep() {
        return xStep;
    }

    public void setxStep(double xStep) {
        this.xStep = xStep;
    }

    public int getValuesPerPoint() {
        return valuesPerPoint;
    }

    public void setValuesPerPoint(int valuesPerPoint) {
        this.valuesPerPoint = valuesPerPoint;
    }

    public String getName() {
        return timeSeries.getName();
    }

    public Long getFrom() {
        return timeSeries.getFrom();
    }

    public Long getTo() {
        return timeSeries.getTo();
    }

    public Double[] getValues() {
        return timeSeries.getValues();
    }

    public int getStep() {
        return timeSeries.getStep();
    }

    public Object getOption(TimeSeriesOption option) {
        return timeSeries.getOption(option);
    }

    public boolean hasOption(TimeSeriesOption option) {
        return timeSeries.hasOption(option);
    }

    public void setOption(TimeSeriesOption option, Object value) {
        timeSeries.setOption(option, value);
    }

    public void addOption(TimeSeriesOption option) {
        timeSeries.addOption(option);
    }


    //todo: implement in a more reasonable way
    public Double[] getConsolidatedValues() {
        if (valuesPerPoint <= 1) return timeSeries.getValues();

        List<Double> consolidated = new ArrayList<>();
        List<Double> buffer = new ArrayList<>();

        for(Double value : timeSeries.getValues()) {
            buffer.add(value);
            if (buffer.size() == valuesPerPoint) {
                buffer.removeAll(Collections.singleton((Double) null));
                if (buffer.size() > 0 ) {
                    consolidated.add(CollectionUtils.average(buffer));
                } else {
                    consolidated.add(null);
                }
                buffer.clear();
            }
        }

        buffer.removeAll(Collections.singleton((Double) null));
        if (buffer.size() > 0 ) {
            consolidated.add(CollectionUtils.average(buffer));
        } else {
            consolidated.add(null);
        }


        Double[] result = new Double[consolidated.size()];
        return consolidated.toArray(result);
    }

}
