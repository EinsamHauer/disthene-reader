package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.CollectionUtils;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

        long window = ((Double) arguments.get(1)).longValue();

        int length = processedArguments.get(0).getValues().length;

        for (TimeSeries ts : processedArguments) {
            Queue<Double> queue = new LinkedList<>();
            Double[] values = new Double[length];

            for (int i = 0; i < length; i++) {
                if (queue.size() == 0) {
                    values[i] = ts.getValues()[i];
                } else {
                    values[i] = CollectionUtils.median(queue);
                }

                if (ts.getValues()[i] != null) {
                    queue.offer(ts.getValues()[i]);
                }

                if (queue.size() > window) {
                    queue.remove();
                }
            }

            ts.setValues(values);
            ts.setName("movingMedian(" + ts.getName() + "," + window + ")");
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 2) throw new InvalidArgumentException("movingMedian: number of arguments is " + arguments.size() + ". Must be two.");
        if (!(arguments.get(0) instanceof PathTarget)) throw new InvalidArgumentException("movingMedian: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("movingMedian: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");
    }
}
