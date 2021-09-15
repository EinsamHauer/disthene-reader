package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.DateTimeUtils;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.*;

/**
 * @author Andrei Ivanov
 */
public abstract class MovingFunction extends DistheneFunction {
    
    MovingFunction(String text, String name) {
        super(text, name);
    }
    
    /**
     * Desc :
     *   This will return moving window size.
     *   Input for window can be nummeric or string like as "1d".
     *   So, when input is string, this change to numeric value and return it.
     *
     * @param windowObject : window object which can be numeric or string.
     * @param step : metric step (interval)
     * @return : windows size
     */
    private long getMovingWindowSize(Object windowObject, int step) {
        if (arguments.get(1) instanceof Double) {
            return ((Double) arguments.get(1)).longValue();
        }
        
        // When windowObject is string,
        return Math.abs(DateTimeUtils.parseTimeOffset((String) arguments.get(1))) / step;
    }
    
    @Override
    public void checkArguments() throws InvalidArgumentException {
    }
    
    abstract public Double operation(ArrayList<Double> movingMetrics);
    
    @Override
    public List <TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
    
        List<TimeSeries> storedMetrics = evaluator.eval((Target) arguments.get(0));
    
        if (storedMetrics.size() == 0) return new ArrayList<>();
    
        if (!TimeSeriesUtils.checkAlignment(storedMetrics)) {
            throw new TimeSeriesNotAlignedException();
        }
    
        int step = storedMetrics.get(0).getStep();
    
        // need to get window in number of data points
        long window = getMovingWindowSize(arguments.get(1), step);
    
        List<TimeSeries> previousMetrics = evaluator.eval(((Target) arguments.get(0)).previous((window) *step));
    
        int previousMetricPos = 0;
    
        for(TimeSeries timeSeries : storedMetrics) {
            Double [] metrics = timeSeries.getValues();
            List<Double> newValues = new ArrayList<>();
            Double [] previousMetric = previousMetrics.get(previousMetricPos++).getValues();
            ArrayList<Double> movingMetrics = new ArrayList <>(Arrays.asList(Arrays.copyOf(previousMetric, previousMetric.length)));
            for (Double metric : metrics) {
                /////////////////////////
                //collect data from movingMetrics
                newValues.add(operation(movingMetrics));
    
                /////////////////////////
                // change movingMetrics
                // push new data
                movingMetrics.add(metric);
                // remove first data
                movingMetrics.remove(0);
            }

            timeSeries.setValues(newValues.toArray(new Double[metrics.length]));
            timeSeries.setName(this.name + "(" + timeSeries.getName() + ", " + window + ")");
        }
    
        return storedMetrics;
    }
    
}
