package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class TimeStackFunction extends DistheneFunction {

    public TimeStackFunction(String text) {
        super(text, "timeStack");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        // parse offset
        long offset = DateTimeUtils.parseTimeOffset((String) arguments.get(1));
        int startIndex = ((Double) arguments.get(2)).intValue();
        int endIndex = ((Double) arguments.get(3)).intValue();


        List<TimeSeries> resultList = new ArrayList<>();
        // todo: we will experience some problems if this resolution doesn't exist anymore in the past... take care of this corner case!
        while (startIndex <= endIndex) {
            List<TimeSeries> processedArguments = evaluator.eval(((Target) arguments.get(0)).shiftBy(- offset * startIndex));
            for (TimeSeries ts : processedArguments) {
                ts.setFrom(ts.getFrom() - offset);
                ts.setTo(ts.getTo() - offset);
                ts.setName("timeShift(" + ts.getName() + "," + arguments.get(1) + "," + startIndex + ")");
                resultList.add(ts);
            }

            startIndex++;
        }

        return resultList;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 4) throw new InvalidArgumentException("timeStack: number of arguments is " + arguments.size() + ". Must be four.");
        // argument cannot be a result of another function - it's not clear how to evaluate it in that case
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("timeStack: argument is " + arguments.get(0).getClass().getName() + ". Must be series wildcard");
        if (!DateTimeUtils.testTimeOffset((String) arguments.get(1))) throw new InvalidArgumentException("timeStack: shift cannot be parsed (" + arguments.get(1) + ")");
        if (!(arguments.get(2) instanceof Double)) throw new InvalidArgumentException("timeStack: argument is " + arguments.get(0).getClass().getName() + ". Must be a number");
        if (!(arguments.get(3) instanceof Double)) throw new InvalidArgumentException("timeStack: argument is " + arguments.get(0).getClass().getName() + ". Must be a number");
    }
}
