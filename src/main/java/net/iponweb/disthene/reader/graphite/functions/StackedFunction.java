package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.beans.TimeSeriesOption;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;

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
        if (position == 0) return argument instanceof Target;
        if (position == 1) return argument instanceof Double;

        return false;
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        for (TimeSeries ts : processedArguments) {
            ts.setOption(TimeSeriesOption.DASHED, arguments.size() == 1 ? new Float(5) : new Float((Double) arguments.get(1)));
            ts.setName("dashed(" + ts.getName() + ")");
        }

        return processedArguments;
    }
}
