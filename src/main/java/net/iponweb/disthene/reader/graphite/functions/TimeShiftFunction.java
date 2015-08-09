package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.graphite.functions.DistheneFunction;
import net.iponweb.disthene.reader.utils.DateTimeUtils;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class TimeShiftFunction extends DistheneFunction {

    public TimeShiftFunction(String text) {
        super(text, "timeShift");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        // parse offset
        long offset = DateTimeUtils.parseTimeOffset((String) arguments.get(1));

        List<TimeSeries> processedArguments = new ArrayList<>();
        // apply shift to pathTarget
        // todo: we will experience some problems if this resolution doesn't exist anymore in the past... take care of this corner case!
        processedArguments.addAll(evaluator.eval(((Target) arguments.get(0)).shiftBy(-offset)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        for (TimeSeries ts : processedArguments) {
            ts.setFrom(ts.getFrom() - offset);
            ts.setTo(ts.getTo() - offset);
            ts.setName("timeShift(" + ts.getName() + "," + arguments.get(1) + ")");
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 3 || arguments.size() < 2) throw new InvalidArgumentException("timeShift: number of arguments is " + arguments.size() + ". Must be two.");
        // argument cannot be a result of another function - it's not clear how to evaluate it in that case
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("timeShift: argument is " + arguments.get(0).getClass().getName() + ". Must be series wildcard");
        if (!DateTimeUtils.testTimeOffset((String) arguments.get(1))) throw new InvalidArgumentException("timeShift: shift cannot be parsed (" + arguments.get(1) + ")");
    }
}
