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
public class MinMaxFunction extends DistheneFunction {

    public MinMaxFunction(String text) {
        super(text, "minMax");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        int length = processedArguments.get(0).getValues().length;

        for (TimeSeries ts : processedArguments) {
            List<Double> valuesArray = Arrays.asList(ts.getValues());
            Double min = CollectionUtils.min(valuesArray);
            Double max = CollectionUtils.max(valuesArray);
            if (min != null && max != null) {
                Double range = max - min;
                for (int i = 0; i < length; i++) {
                    if (valuesArray[i] != null ) {
                        ts.getValues()[i] = (valuesArray[i] - min) / range;
                    }
                }
            }
            setResultingName(ts);
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 1) throw new InvalidArgumentException("minMax: number of arguments is " + arguments.size() + ". Must be one.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("minMax: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
    }
}