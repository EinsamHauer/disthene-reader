package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.beans.TimeSeriesOption;
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
public class DashedFunction extends DistheneFunction {

    public DashedFunction(String text) {
        super(text, "dashed");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        //todo dash length constant
        for (TimeSeries ts : processedArguments) {
            ts.setOption(TimeSeriesOption.DASHED, arguments.size() == 1 ? new Float(5) : new Float((Double) arguments.get(1)));
            setResultingName(ts);
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 2 || arguments.size() == 0) throw new InvalidArgumentException("dashed: number of arguments is " + arguments.size() + ". Must be one or two.");

        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("dashed: 1st argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (arguments.size() > 1 && !(arguments.get(1) instanceof Double))  throw new InvalidArgumentException("dashed: 2ns argument is " + arguments.get(0).getClass().getName() + ". Must be a float number");

    }
}
