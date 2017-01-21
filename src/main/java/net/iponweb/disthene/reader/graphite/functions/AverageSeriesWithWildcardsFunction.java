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

import java.util.*;

/**
 * @author Andrei Ivanov
 */
public class AverageSeriesWithWildcardsFunction extends DistheneFunction {


    public AverageSeriesWithWildcardsFunction(String text) {
        super(text, "averageSeriesWithWildcards");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        int[] positions = new int[arguments.size() - 1];
        for (int i = 1; i < arguments.size(); i++) {
            positions[i - 1] = ((Double) arguments.get(i)).intValue();
        }

        // put series into buckets according to position
        Map<String, List<TimeSeries>> buckets = new HashMap<>();

        for (TimeSeries ts : processedArguments) {
            String bucketName = getBucketName(ts.getName(), positions);
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
                values[i] = CollectionUtils.average(points);
            }

            timeSeries.setValues(values);
            timeSeries.setName(bucket.getKey());
            resultTimeSeries.add(timeSeries);
        }

        return resultTimeSeries;
    }

    private String getBucketName(String name, int[] positions) {
        String[] split = name.split("\\.");
        for (int position : positions) {
            if (position < split.length) {
                split[position] = null;
            }
        }
        return Joiner.on(".").skipNulls().join(split);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 2)
            throw new InvalidArgumentException("averageSeriesWithWildcards: number of arguments is " + arguments.size() + ". Must be at least two.");

        if (!(arguments.get(0) instanceof PathTarget))
            throw new InvalidArgumentException("averageSeriesWithWildcards: argument is " + arguments.get(0).getClass().getName() + ". Must be series");

        for (int i = 1; i < arguments.size(); i++) {
            if (!(arguments.get(i) instanceof Double))
                throw new InvalidArgumentException("averageSeriesWithWildcards: argument " + i + " is " + arguments.get(i).getClass().getName() + ". Must be a number");
        }
    }
}