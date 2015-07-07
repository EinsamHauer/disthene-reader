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
public class MultiplySeriesFunction extends DistheneFunction {


    public MultiplySeriesFunction(String text) {
        super(text, "multiplySeries");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        for(Object target : arguments) {
            processedArguments.addAll(evaluator.eval((Target) target));
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
            values[i] = 1.;
            for(TimeSeries ts : processedArguments) {
                if (ts.getValues()[i] == null) {
                    values[i] = null;
                    break;
                } else {
                    values[i] *= ts.getValues()[i];
                }
            }
        }

        resultTimeSeries.setValues(values);

        return Collections.singletonList(resultTimeSeries);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 1) throw new InvalidArgumentException("multiplySeries: number of arguments is " + arguments.size() + ". Must be at least one.");

        for(Object argument : arguments) {
            if (!(argument instanceof Target)) throw new InvalidArgumentException("multiplySeries: argument is " + argument.getClass().getName() + ". Must be series");
        }
    }
}
