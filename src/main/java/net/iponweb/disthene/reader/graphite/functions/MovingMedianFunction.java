package net.iponweb.disthene.reader.graphite.functions;

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

import java.util.*;

/**
 * @author Andrei Ivanov
 */
public class MovingMedianFunction extends DistheneFunction {

    public MovingMedianFunction(String text) {
        super(text, "movingMedian");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        int step = processedArguments.get(0).getStep();
        int length = processedArguments.get(0).getValues().length;

        // need to get window in number of data points
        long window;
        if (arguments.get(1) instanceof Double) {
            window = ((Double) arguments.get(1)).longValue();
        } else {
            long offset = Math.abs(DateTimeUtils.parseTimeOffset((String) arguments.get(1)));
            window = offset / step;
        }


        List<TimeSeries> bootstrapped = evaluator.bootstrap((Target) arguments.get(0), processedArguments, window * step);
        if (bootstrapped.size() == 0) return new ArrayList<>();

        int bootstrappedLength = bootstrapped.get(0).getValues().length;

        for (int i = 0; i < processedArguments.size(); i++) {
            Queue<Double> queue = new LinkedList<>();
            Double[] values = new Double[bootstrappedLength];
            TimeSeries bts = bootstrapped.get(i);

            for (int j = 0; j < bootstrappedLength; j++) {
                if (queue.size() == 0) {
                    values[j] = bts.getValues()[j];
                } else {
                    values[j] = CollectionUtils.median(queue);
                }

                if (bts.getValues()[j] != null) {
                    queue.offer(bts.getValues()[j]);
                }

                if (queue.size() > window) {
                    queue.remove();
                }
            }

            processedArguments.get(i).setValues(Arrays.copyOfRange(values, bootstrappedLength - length, bootstrappedLength));
            processedArguments.get(i).setName("movingMedian(" + processedArguments.get(i).getName() + "," + window + ")");
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 2) throw new InvalidArgumentException("movingMedian: number of arguments is " + arguments.size() + ". Must be two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("movingMedian: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double) && !(arguments.get(1) instanceof String)) throw new InvalidArgumentException("movingMedian: argument is " + arguments.get(1).getClass().getName() + ". Must be a number or a string");
    }
}
