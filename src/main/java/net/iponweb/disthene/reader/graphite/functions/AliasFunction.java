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
public class AliasFunction extends DistheneFunction {

    public AliasFunction(String text) {
        super(text, "alias");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        String alias = (String) arguments.get(1);

        for (TimeSeries ts : processedArguments) {
            ts.setName(alias);
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 2) throw new InvalidArgumentException("alias: number of arguments is " + arguments.size() + ". Must be two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("alias: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof String)) throw new InvalidArgumentException("alias: argument is " + arguments.get(1).getClass().getName() + ". Must be a string");
    }
}
