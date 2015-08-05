package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.MultipleDivisorsException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Andrei Ivanov
 */
public class DivideSeriesFunction extends DistheneFunction {

    public DivideSeriesFunction(String text) {
        super(text, "divideSeries");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> dividends = new ArrayList<>();
        dividends.addAll(evaluator.eval((Target) arguments.get(0)));

        if (dividends.size() == 0) return Collections.emptyList();

        List<TimeSeries> divisors = evaluator.eval((Target) arguments.get(1));
        if (divisors.size() == 0) return Collections.emptyList();
        if (divisors.size() != 1) throw new MultipleDivisorsException();
        TimeSeries divisor = divisors.get(0);


        List<TimeSeries> tmp = new ArrayList<>();
        tmp.addAll(dividends);
        tmp.add(divisor);
        if (!TimeSeriesUtils.checkAlignment(tmp)) {
            throw new TimeSeriesNotAlignedException();
        }

        List<TimeSeries> result = new ArrayList<>();
        int length = divisor.getValues().length;
        long from = divisor.getFrom();
        long to = divisor.getTo();
        int step = divisor.getStep();

        for (TimeSeries ts : dividends) {
            Double[] values = new Double[length];
            TimeSeries resultTimeSeries = new TimeSeries(getText(), from, to, step);

            for (int i = 0; i < length; i++) {

                if (divisor.getValues()[i] == null || ts.getValues()[i] == null || divisor.getValues()[i] == 0) {
                    values[i] = null;
                } else {
                    values[i] = ts.getValues()[i] / divisor.getValues()[i];
                }
            }

            resultTimeSeries.setValues(values);
            result.add(resultTimeSeries);
        }

        return result;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 2 || arguments.size() < 1) throw new InvalidArgumentException("divideSeries: number of arguments is " + arguments.size() + ". Must be 2.");

        for(Object argument : arguments) {
            if (!(argument instanceof Target)) throw new InvalidArgumentException("divideSeries: argument is " + argument.getClass().getName() + ". Must be series");
        }
    }
}
