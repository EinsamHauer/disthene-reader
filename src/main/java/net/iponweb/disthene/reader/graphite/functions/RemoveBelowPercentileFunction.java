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
public class RemoveBelowPercentileFunction extends DistheneFunction {


    public RemoveBelowPercentileFunction(String text) {
        super(text, "removeBelowPercentile");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        double threshold = (Double) arguments.get(1);
        int length = processedArguments.get(0).getValues().length;

        for(TimeSeries ts : processedArguments) {
            Double percentile = CollectionUtils.percentile(Arrays.asList(ts.getValues()), threshold, false);

            if (percentile != null) {
                for (int i = 0; i < length; i++) {
                    if (ts.getValues()[i] != null && ts.getValues()[i] < percentile) {
                        ts.getValues()[i] = null;
                    }
                }
            }
            ts.setName("removeAboveValue(" + ts.getName() + "," + threshold + ")");
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 2) throw new InvalidArgumentException("removeBelowPercentile: number of arguments is " + arguments.size() + ". Must be one.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("removeBelowPercentile: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("removeBelowPercentile: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");
    }
}