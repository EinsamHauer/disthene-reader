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
public class GroupFunction extends DistheneFunction {


    public GroupFunction(String text) {
        super(text, "group");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        for(Object target : arguments) {
            processedArguments.addAll(evaluator.eval((Target) target));
        }

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 1) throw new InvalidArgumentException("group: number of arguments is " + arguments.size() + ". Must be at least one.");

        for(Object argument : arguments) {
            if (!(argument instanceof Target)) throw new InvalidArgumentException("group: argument is " + argument.getClass().getName() + ". Must be series");
        }
    }
}
