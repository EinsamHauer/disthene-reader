package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.Target;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Andrei Ivanov
 * @author coupang (swhors@coupang.com)
 */
public class MovingMedianFunction extends MovingFunction{

    public MovingMedianFunction(String text) {
        super(text, "movingMedian");
    }

    /**
     * Desc :
     *   This will return median value from ArrayList.
     *
     * @param movingMetrics : list array for moving_metrics
     * @return : median value
     */
    private Double median (ArrayList<Double> movingMetrics){
        ArrayList<Double> newMetric = new ArrayList <>();

        // 1) Remove Null Values
        movingMetrics.forEach( metric -> {
            if (metric != null) newMetric.add(metric);
        });

        // 2) when there is no data, return null
        if (newMetric.size() == 0) return null;

        // 3) sort new metric array
        Collections.sort(newMetric);

        // 4) Set median index
        int index = newMetric.size()/2;

        // 5) return
        return newMetric.get(index);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 2)
            throw new InvalidArgumentException("movingMedian: number of arguments is " + arguments.size() + ". Must be two.");
        if (!(arguments.get(0) instanceof Target))
            throw new InvalidArgumentException("movingMedian: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double) && !(arguments.get(1) instanceof String))
            throw new InvalidArgumentException("movingMedian: argument is " + arguments.get(1).getClass().getName() + ". Must be a number or a string");
    }

    @Override
    public Double operation(ArrayList <Double> movingMetrics) {
        return median(movingMetrics);
    }
}
