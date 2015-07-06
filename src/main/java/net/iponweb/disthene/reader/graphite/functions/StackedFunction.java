package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.beans.TimeSeriesOption;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class StackedFunction extends DistheneFunction {

    public StackedFunction(String text) {
        super(text);
    }

    @Override
    protected boolean checkArgument(int position, Object argument) {
        return argument instanceof Target;
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        // all args are targets
        List<TimeSeries> processedArguments = new ArrayList<>();
        for(Object target : arguments) {
            processedArguments.addAll(evaluator.eval((Target) target));
        }

        // check that all aligned
        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        if (processedArguments.size() == 0) return new ArrayList<>();

        for(TimeSeries ts : processedArguments) {
            ts.addOption(TimeSeriesOption.STACKED);
            ts.setName("stacked(" + ts.getName() + ")");
        }

        return processedArguments;
    }
}
