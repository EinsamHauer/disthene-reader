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
public class SecondYAxisFunction extends DistheneFunction {

    public SecondYAxisFunction(String text) {
        super(text, "secondYAxis");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        for(TimeSeries ts : processedArguments) {
            ts.addOption(TimeSeriesOption.SECOND_Y_AXIS);
            setResultingName(ts);
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 1) throw new InvalidArgumentException("secondYAxis: number of arguments is " + arguments.size() + ". Must be one.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("secondYAxis: argument is " + arguments.get(0).getClass().getName() + ". Must be series");

    }
}
