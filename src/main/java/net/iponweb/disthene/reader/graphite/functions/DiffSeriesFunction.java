package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class DiffSeriesFunction extends DistheneFunction {


    public DiffSeriesFunction(String text) {
        super(text, "diffSeries");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        for(Object target : arguments) {
            processedArguments.addAll(evaluator.eval((Target) target));
        }

        if (processedArguments.size() == 0) return new ArrayList<>();
        if (processedArguments.size() == 1) return processedArguments;

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        TimeSeries minuend = processedArguments.get(0);
        List<TimeSeries> subtrahends = processedArguments.subList(1, processedArguments.size());

        int length = minuend.getValues().length;
        long from = minuend.getFrom();
        long to = minuend.getTo();
        int step = minuend.getStep();

        TimeSeries resultTimeSeries = new TimeSeries(getText(), from, to, step);
        Double[] values = minuend.getValues();


        for (TimeSeries ts : subtrahends) {
            for (int i = 0; i < length; i++) {
                if (values[i] != null && ts.getValues()[i] != null) {
                    values[i] -= ts.getValues()[i];
                }
            }
        }

        resultTimeSeries.setValues(values);
        return Collections.singletonList(resultTimeSeries);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 2) throw new InvalidArgumentException("diffSeries: number of arguments is " + arguments.size() + ". Must be at least 2.");

        for(Object argument : arguments) {
            if (!(argument instanceof Target)) throw new InvalidArgumentException("diffSeries: argument is " + argument.getClass().getName() + ". Must be series");
        }
    }
}
