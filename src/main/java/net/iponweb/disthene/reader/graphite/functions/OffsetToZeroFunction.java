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
public class OffsetToZeroFunction extends DistheneFunction {

    public OffsetToZeroFunction(String text) {
        super(text, "offsetToZero");
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
            Double offset = CollectionUtils.min(Arrays.asList(ts.getValues()));
            if (offset != null) {
                for (int i = 0; i < length; i++) {
                    if (ts.getValues()[i] != null ) {
                        ts.getValues()[i] -= offset;
                    }
                }
            }
            setResultingName(ts);
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 1 || arguments.size() == 0) throw new InvalidArgumentException("offsetToZero: number of arguments is " + arguments.size() + ". Must be 1.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("offsetToZero: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
    }
}
