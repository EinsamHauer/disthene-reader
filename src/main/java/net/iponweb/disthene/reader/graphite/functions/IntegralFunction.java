package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class IntegralFunction extends DistheneFunction {


    public IntegralFunction(String text) {
        super(text, "integral");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        long from = processedArguments.get(0).getFrom();
        long to = processedArguments.get(0).getTo();
        int step = processedArguments.get(0).getStep();
        int length = processedArguments.get(0).getValues().length;

        List<TimeSeries> resultTimeSeriesList = new ArrayList<>();
        for(TimeSeries ts : processedArguments) {
            double integral = 0;
            Double[] values = new Double[length];

            for (int i = 0; i < length; i++) {
                if (ts.getValues()[i] != null) {
                    integral += ts.getValues()[i];
                }
                values[i] = integral;
            }

            TimeSeries result = new TimeSeries(getResultingName(ts), from, to, step);
            result.setValues(values);

            resultTimeSeriesList.add(result);
        }

        return resultTimeSeriesList;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 1) throw new InvalidArgumentException("integral: number of arguments is 0. Must be at least one.");

        for(Object argument : arguments) {
            if (!(argument instanceof Target)) throw new InvalidArgumentException("integral: argument is " + argument.getClass().getName() + ". Must be series");
        }
    }
}
