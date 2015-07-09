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
public class MinimumAboveFunction extends DistheneFunction {


    public MinimumAboveFunction(String text) {
        super(text, "minimumAbove");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        double threshold = (Double) arguments.get(1);

        List<TimeSeries> result = new ArrayList<>();

        for(TimeSeries ts : processedArguments) {
            Double max = CollectionUtils.min(Arrays.asList(ts.getValues()));
            if (max != null && max > threshold) {
                result.add(ts);
            }
        }

        return result;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 2) throw new InvalidArgumentException("minimumAbove: number of arguments is " + arguments.size() + ". Must be one.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("minimumAbove: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("minimumAbove: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");
    }
}