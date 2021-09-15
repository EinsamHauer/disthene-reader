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
 * Takes one metric or a wildcard seriesList, followed by a quoted string with the length of time
 * (See ``from / until`` in the render\_api_ for examples of time formats). Also takes a start
 * multiplier and end multiplier for the length of time-
 *
 * Create a seriesList which is composed the orginal metric series stacked with time shifts
 * starting time shifts from the start multiplier through the end multiplier.
 *
 * Useful for looking at history, or feeding into averageSeries or stddevSeries.
 *
 * Example::
 *
 * # create a series for today and each of the previous 7 days
 *    &target=timeStack(Sales.widgets.largeBlue,"1d",0,7)
 */
public class TimeStackFunction extends DistheneFunction {

    public TimeStackFunction(String text) {
        super(text, "timeStack");
    }

    private List<TimeSeries> timeStack(TargetEvaluator evaluator,
                                       long delta,
                                       int startIndex,
                                       int endIndex) throws EvaluationException {
        List<TimeSeries> newSeries = new ArrayList <>();

        if (startIndex < endIndex) {
            for (int shift = startIndex; shift < endIndex; shift++) {
                List <TimeSeries> shiftedSeriesList = evaluator.eval(((Target) arguments.get(0)).shiftBy(-(delta * shift)));
                int finalShift = shift;
                shiftedSeriesList.forEach(series -> {
                    series.setName("timeShift(" + series.getName() + "," + arguments.get(1) + "," + finalShift + ")");
                    series.setFrom(this.from);
                    series.setTo(this.to);
                });
                newSeries.addAll(shiftedSeriesList);
            }
        }

        return newSeries;
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        // parse offset
        long offset = DateTimeUtils.parseTimeOffset((String) arguments.get(1));
        int startIndex = ((Double) arguments.get(2)).intValue();
        int endIndex = ((Double) arguments.get(3)).intValue();

        return timeStack(evaluator, offset, startIndex, endIndex  );
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
