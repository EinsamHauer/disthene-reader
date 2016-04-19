package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.graphite.utils.GraphiteUtils;
import net.iponweb.disthene.reader.graphite.utils.UnitSystem;
import net.iponweb.disthene.reader.graphite.utils.ValueFormatter;
import net.iponweb.disthene.reader.utils.CollectionUtils;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class CactiStyleFunction extends DistheneFunction {

    public CactiStyleFunction(String text) {
        super(text, "cactiStyle");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        UnitSystem unitSystem = arguments.size() > 1 ? UnitSystem.valueOf(((String) arguments.get(1)).replaceAll("^\"|\"$", "").toUpperCase()) : UnitSystem.NONE;

        ValueFormatter formatter = getContext().getFormatter();

        for (TimeSeries ts : processedArguments) {
            List<Double> valuesArray = Arrays.asList(ts.getValues());
            Double last = CollectionUtils.last(valuesArray);
            Double min = CollectionUtils.min(valuesArray);
            Double max = CollectionUtils.max(valuesArray);

            ts.setName(ts.getName() + " Current:" + formatter.formatValue(last, unitSystem) + "   Max:" + formatter.formatValue(max, unitSystem) + "   Min:" + formatter.formatValue(min, unitSystem));
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 1 || arguments.size() > 2) throw new InvalidArgumentException("cactiStyle: number of arguments is " + arguments.size() + ". Must be one or two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("cactiStyle: argument is " + arguments.get(0).getClass().getName() + ". Must be series");

        if (arguments.size() > 1) {
            if (!(arguments.get(1) instanceof String)) throw new InvalidArgumentException("cactiStyle: argument is " + arguments.get(1).getClass().getName() + ". Must be a string");
            try {
                UnitSystem.valueOf(((String) arguments.get(1)).toUpperCase().replaceAll("^\"|\"$", ""));
            } catch (IllegalArgumentException e) {
                throw new InvalidArgumentException("cactiStyle: unknown unit system.");
            }
        }
    }
}
