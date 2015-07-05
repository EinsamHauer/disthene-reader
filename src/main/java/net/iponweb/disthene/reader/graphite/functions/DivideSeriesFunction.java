package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.MultipleDivisorsException;
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
public class DivideSeriesFunction extends DistheneFunction {

    public DivideSeriesFunction(String text) {
        super(text);
    }

    //todo: check arguments as a whole
    @Override
    protected boolean checkArgument(int position, Object argument) {
        return argument instanceof Target;
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        // The logic here is:
        // All arguments but first are dividends
        // The last one is divisor

        List<TimeSeries> dividends = new ArrayList<>();
        for(int i = 0; i < arguments.size() - 1; i++) {
            dividends.addAll(evaluator.eval((Target) arguments.get(i)));
        }
        List<TimeSeries> divisors = evaluator.eval((Target) arguments.get(arguments.size() - 1));

        if (divisors.size() > 1) {
            throw new MultipleDivisorsException();
        }

        TimeSeries divisor = divisors.get(0);

        List<TimeSeries> tmp = new ArrayList<>();
        tmp.addAll(dividends);
        tmp.add(divisor);
        // check that all aligned
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
            TimeSeries resultTimeSeries = new TimeSeries("divideSeries(" + ts.getName() + "," + divisor.getName() + ")", from, to, step);

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
}
