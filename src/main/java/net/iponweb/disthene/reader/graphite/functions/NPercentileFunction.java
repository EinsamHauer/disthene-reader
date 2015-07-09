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
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class NPercentileFunction extends DistheneFunction {

    public NPercentileFunction(String text) {
        super(text, "nPercentile");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        double percentile = ((Double) arguments.get(1)).doubleValue();

        int length = processedArguments.get(0).getValues().length;

        for (TimeSeries ts : processedArguments) {
            Double percentileValue = CollectionUtils.percentile(Arrays.asList(ts.getValues()), percentile, false);

            for (int i = 0; i < length; i++) {
                ts.getValues()[i] = percentileValue;
            }
            ts.setName("nPercentile(" + ts.getName() + "," + percentile + ")");
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 2) throw new InvalidArgumentException("nPercentile: number of arguments is " + arguments.size() + ". Must be two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("nPercentile: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("nPercentile: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");
    }
}
