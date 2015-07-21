package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.CollectionUtils;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class PercentileOfSeriesFunction extends DistheneFunction {


    public PercentileOfSeriesFunction(String text) {
        super(text, "percentileOfSeries");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        Double percentile = (Double) arguments.get(1);
        boolean interpolate = arguments.size() > 2 ? (Boolean) arguments.get(2) : false;

        long from = processedArguments.get(0).getFrom();
        long to = processedArguments.get(0).getTo();
        int step = processedArguments.get(0).getStep();
        int length = processedArguments.get(0).getValues().length;

        TimeSeries resultTimeSeries = new TimeSeries(getText(), from, to, step);
        Double[] values = new Double[length];

        for (int i = 0; i < length; i++) {
            List<Double> points = new ArrayList<>();
            for(TimeSeries ts : processedArguments) {
                points.add(ts.getValues()[i]);
            }

            values[i] = CollectionUtils.percentile(points, percentile, interpolate);
        }

        resultTimeSeries.setValues(values);

        return Collections.singletonList(resultTimeSeries);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 2 || arguments.size() > 3) throw new InvalidArgumentException("percentileOfSeries: number of arguments is " + arguments.size() + ". Must be two or three.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("percentileOfSeries: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("percentileOfSeries: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");
        if (arguments.size() == 3 && !(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("percentileOfSeries: argument is " + arguments.get(2).getClass().getName() + ". Must be boolean");
    }
}
