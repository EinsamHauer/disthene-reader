package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.CollectionUtils;
import net.iponweb.disthene.reader.utils.DateTimeUtils;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.*;

/**
 * @author Andrei Ivanov
 */
public class MovingAverageFunction extends MovingFunction {

    public MovingAverageFunction(String text) {
        super(text, "movingAverage");
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 2) throw new InvalidArgumentException("movingAverage: number of arguments is " + arguments.size() + ". Must be two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("movingAverage: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double) && !(arguments.get(1) instanceof String)) throw new InvalidArgumentException("movingAverage: argument is " + arguments.get(1).getClass().getName() + ". Must be a number or a string");
    }

    /**
     * Desc :
     *   This will return average value from ArrayList.
     *
     * @param movingMetrics : list array for moving_metrics
     * @return : median value
     */
    private Double average(ArrayList<Double> movingMetrics){
        ArrayList<Double> newMetric = new ArrayList <>();

        // 1) Remove Null Values
        movingMetrics.forEach( metric -> {
            if (metric != null) newMetric.add(metric);
        });

        // 2) when there is no data, return null
        if (newMetric.size() == 0) return null;

        // 3) sort new metric array
        return CollectionUtils.average(newMetric);
    }

    @Override
    public Double operation(ArrayList <Double> movingMetrics) {
        return average(movingMetrics);
    }
}
