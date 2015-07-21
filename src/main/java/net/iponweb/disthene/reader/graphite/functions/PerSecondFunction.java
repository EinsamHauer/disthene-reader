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
public class PerSecondFunction extends DistheneFunction {

    public PerSecondFunction(String text) {
        super(text, "perSecond");
    }

    //todo: this implementation comes from graphite, but it doesn't seem right. Why cut at zero??
    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        Double maxValue = arguments.size() > 1 ? (Double) arguments.get(1) : null;

        int length = processedArguments.get(0).getValues().length;

        for (TimeSeries ts : processedArguments) {
            Double[] values = new Double[length];
            Double previous = null;
            for (int i = 0; i < length; i++) {
                if (previous != null && ts.getValues()[i] != null && (ts.getValues()[i] - previous > 0)) {
                    values[i] = (ts.getValues()[i] - previous) / ts.getStep();
                } else if (previous != null && ts.getValues()[i]!= null && maxValue != null && maxValue >= ts.getValues()[i]) {
                    values[i] = (maxValue - previous + ts.getValues()[i] + 1) / ts.getStep();
                }

                previous = ts.getValues()[i];
            }

            ts.setValues(values);
            if (maxValue != null) {
                ts.setName("perSecond(" + ts.getName() + "," + maxValue + ")");
            } else {
                setResultingName(ts);
            }
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 2 || arguments.size() < 1) throw new InvalidArgumentException("perSecond: number of arguments is " + arguments.size() + ". Must be two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("perSecond: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (arguments.size()> 1 && !(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("perSecond: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");
    }
}
