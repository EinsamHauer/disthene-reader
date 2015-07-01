package net.iponweb.disthene.reader.graph;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.beans.TimeSeriesOption;

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

}
