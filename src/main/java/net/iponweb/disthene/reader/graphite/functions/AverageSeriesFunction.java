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
public class AverageSeriesFunction extends DistheneFunction {


    public AverageSeriesFunction(String text) {
        super(text, "averageSeries");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        for (Object argument : arguments) {
            processedArguments.addAll(evaluator.eval((Target) argument));
        }

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }


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

            values[i] = CollectionUtils.average(points);
        }

        resultTimeSeries.setValues(values);

        return Collections.singletonList(resultTimeSeries);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() == 0) throw new InvalidArgumentException("averageSeries: number of arguments is " + arguments.size() + ". Must be at least one.");

        for (int i = 0; i < arguments.size() - 1; i++) {
            if (!(arguments.get(i) instanceof Target))
                throw new InvalidArgumentException("averageSeries: argument is " + arguments.get(i).getClass().getName() + ". Must be series");
        }
    }
}