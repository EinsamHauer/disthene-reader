package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.*;

/**
 * @author Andrei Ivanov
 */
public class SortByNameFunction extends DistheneFunction {

    private boolean sortSeries = true;

    public SortByNameFunction(String text) {
        super(text, "sortByName");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {

        List<TimeSeries> processedArguments = new ArrayList<>(evaluator.eval((Target) arguments.get(0)));

        if (arguments.size() == 2) {
            sortSeries = arguments.get(1).toString().equalsIgnoreCase("true");
        }

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        if (sortSeries) {
            SortedMap <String, List <TimeSeries>> sorted = new TreeMap <>();

            for (TimeSeries ts : processedArguments) {
                sorted.computeIfAbsent(ts.getName(), k -> new ArrayList<>());
                sorted.get(ts.getName()).add(ts);
            }

            List <TimeSeries> result = new ArrayList <>();

            for (Map.Entry <String, List <TimeSeries>> entry : sorted.entrySet()) {
                result.addAll(entry.getValue());
            }

            return result;
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 2) throw new InvalidArgumentException("sortByName: number of arguments is " + arguments.size() + ". Must be one or two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("sortByName: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
    }
}
