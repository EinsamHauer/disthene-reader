package net.iponweb.disthene.reader.graphite.functions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.CollectionUtils;
import net.iponweb.disthene.reader.utils.DateTimeUtils;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

/**
 * @author Andrei Ivanov
 *
 * @author coupang (swhors@coupang.com)
 */
public class StdevFunction extends DistheneFunction {

    public StdevFunction(String text) {
        super(text, "stdev");
    }

    private TimeSeries calculateStdev(TimeSeries timeSeries, int points, float windowTolerance) {
        int index = 0;
        int validPoints = 0;
        float currentSum = 0;
        float currentSumOfSquares = 0;

        Double[] stddevVal = new Double[timeSeries.getValues().length];

        for (Double val : timeSeries.getValues()) {
            boolean bootstrapping = true;
            Double droppedValue = null;
            double deviation;

            Float newVal = (val == null ? null : val.floatValue());

            if (index >= points) {
                bootstrapping = false;
                droppedValue = timeSeries.getValues()[index - points];
            }

            // Track non-None points in window
            if (! bootstrapping && droppedValue != null) {
                validPoints--;
            }
            if (newVal != null) {
                validPoints++;
            }

            // Remove the value that just dropped out of the window
            if (! bootstrapping && droppedValue != null) {
                currentSum -= droppedValue;
                currentSumOfSquares -= Math.pow(droppedValue, 2);
            }

            // Add in the value that just dropped in the window
            if (newVal != null) {
                currentSum += newVal;
                currentSumOfSquares += Math.pow(newVal, 2);
            }

            if ((validPoints > 0) && (((float) validPoints / points) >= windowTolerance)) {
                deviation = Math.sqrt((validPoints * currentSumOfSquares) - Math.pow(currentSum, 2)) / validPoints;
                stddevVal[index] = deviation;
            } else {
                stddevVal[index] = null;
            }
            index++;
        }
        return new TimeSeries(String.format("stddev(%s,%d)", timeSeries.getName(), points),
                              timeSeries.getFrom(), timeSeries.getFrom(), timeSeries.getStep(), stddevVal);
    }

    private List<TimeSeries> evaluateWithThreeArgs(List<TimeSeries> processedArguments,
                                                   int points, float windowTolerance) {
        List<TimeSeries> newTimeSeries = new ArrayList <>();
        processedArguments.forEach( timeSeries -> newTimeSeries.add(calculateStdev(timeSeries, points, windowTolerance)));
        return newTimeSeries;
    }

    private List<TimeSeries> evaluateWithTwoArgs(List<TimeSeries> processedArguments, int step) {
        // need to get window in number of data points
        long window;

        if (arguments.get(1) instanceof Double) {
            window = ((Double) arguments.get(1)).longValue();
        }
        else {
            long offset = Math.abs(DateTimeUtils.parseTimeOffset((String) arguments.get(1)));
            window = offset / step;
        }
        for (TimeSeries ts : processedArguments) {
            Queue<Double> queue = new LinkedList<>();
            int length = ts.getValues().length;

            Double[] values = new Double[length];

            for (int i = 0; i < length; i++) {
                if (queue.size() == 0) {
                    values[i] = 0.;
                } else {
                    values[i] = CollectionUtils.stdev(queue);
                }

                if (ts.getValues()[i] != null) {
                    queue.offer(ts.getValues()[i]);
                }

                if (queue.size() > window) {
                    queue.remove();
                }
            }

            ts.setValues(values);
            ts.setName("stdev(" + ts.getName() + "," + window + ")");
        }

        return processedArguments;
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = evaluator.eval((Target) arguments.get(0));

        if (processedArguments.size() == 0) return null;

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        if (arguments.size() == 3) {
            int points = ((Double)arguments.get(1)).intValue();
            float windowTolerance = ((Double)arguments.get(2)).floatValue();
            return evaluateWithThreeArgs(processedArguments, points, windowTolerance);
        } else {
            return evaluateWithTwoArgs(processedArguments, processedArguments.get(0).getStep());
        }
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 2 || arguments.size() > 3) throw new InvalidArgumentException("stdev: number of arguments is " + arguments.size() + ". Must be two or three.");
        if (!(arguments.get(0) instanceof PathTarget)) throw new InvalidArgumentException("stdev: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double) && !(arguments.get(1) instanceof String)) throw new InvalidArgumentException("stdev: argument is " + arguments.get(1).getClass().getName() + ". Must be a number or a string");
    }
}
