package net.iponweb.disthene.reader.graphite.functions;

import com.google.common.base.Joiner;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrei Ivanov
 */
public class MultiplySeriesWithWildcardsFunction extends DistheneFunction {


    public MultiplySeriesWithWildcardsFunction(String text) {
        super(text, "multiplySeriesWithWildcards");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();

        for (int i = 0; i < arguments.size() - 1; i++) {
            processedArguments.addAll(evaluator.eval((Target) arguments.get(i)));
        }

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        int position = ((Double) arguments.get(arguments.size() - 1)).intValue();

        // put series into buckets according to position
        Map<String, List<TimeSeries>> buckets = new HashMap<>();

        for (TimeSeries ts : processedArguments) {
            String bucketName = getBucketName(ts.getName(), position);
            if (!buckets.containsKey(bucketName)) buckets.put(bucketName, new ArrayList<TimeSeries>());
            buckets.get(bucketName).add(ts);
        }

        // build new time series now
        long from = processedArguments.get(0).getFrom();
        long to = processedArguments.get(0).getTo();
        int step = processedArguments.get(0).getStep();
        int length = processedArguments.get(0).getValues().length;

        List<TimeSeries> resultTimeSeries = new ArrayList<>();

        for (Map.Entry<String, List<TimeSeries>> bucket : buckets.entrySet()) {
            TimeSeries timeSeries = new TimeSeries(bucket.getKey(), from, to, step);
            Double[] values = new Double[length];

            for (int i = 0; i < length; i++) {
                List<Double> points = new ArrayList<>();
                for (TimeSeries ts : bucket.getValue()) {
                    points.add(ts.getValues()[i]);
                }
                values[i] = CollectionUtils.product(points);
            }

            timeSeries.setValues(values);
            timeSeries.setName(bucket.getKey());
            resultTimeSeries.add(timeSeries);
        }

        return resultTimeSeries;
    }

    private String getBucketName(String name, int position) {
        String[] split = name.split("\\.");
        if (position < split.length) {
            split[position] = null;
        }

        return Joiner.on(".").skipNulls().join(split);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 2)
            throw new InvalidArgumentException("multiplySeriesWithWildcards: number of arguments is " + arguments.size() + ". Must be at least two.");

        for (int i = 0; i < arguments.size() - 1; i++) {
            if (!(arguments.get(i) instanceof PathTarget))
                throw new InvalidArgumentException("multiplySeriesWithWildcards: argument is " + arguments.get(i).getClass().getName() + ". Must be series");
        }
        if (!(arguments.get(arguments.size() - 1) instanceof Double))
            throw new InvalidArgumentException("multiplySeriesWithWildcards: argument is " + arguments.get(arguments.size() - 2).getClass().getName() + ". Must be a number");
    }
}