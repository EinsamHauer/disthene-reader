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
public class OffsetFunction extends DistheneFunction {

    public OffsetFunction(String text) {
        super(text, "offset");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        Double offset = (Double) arguments.get(1);

        int length = processedArguments.get(0).getValues().length;

        for (TimeSeries ts : processedArguments) {
            for (int i = 0; i < length; i++) {
                if (ts.getValues()[i] != null) {
                    ts.getValues()[i] += offset;
                }
            }
            ts.setName("offset(" + ts.getName() + "," + offset + ")");
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 2 || arguments.size() < 1) throw new InvalidArgumentException("offset: number of arguments is " + arguments.size() + ". Must be two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("offset: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("offset: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");
    }
}
