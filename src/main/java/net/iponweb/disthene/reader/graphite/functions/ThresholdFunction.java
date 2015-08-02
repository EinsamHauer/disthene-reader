package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.beans.TimeSeriesOption;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graph.ColorTable;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.CollectionUtils;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class ThresholdFunction extends DistheneFunction {

    public ThresholdFunction(String text) {
        super(text, "threshold");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        Double constant = (Double) arguments.get(0);

        TimeSeries ts = evaluator.getEmptyTimeSeries(from, to);
        CollectionUtils.constant(ts.getValues(), constant);

        if (arguments.size() > 1) {
            ts.setName((String) arguments.get(1));
        } else {
            ts.setName(String.valueOf(constant));
        }

        if (arguments.size() > 2) {
            Color color = ColorTable.getColorByName((String) arguments.get(2));

            if (color != null) {
                ts.setOption(TimeSeriesOption.COLOR, color);
                ts.setOption(TimeSeriesOption.ALPHA, color.getAlpha() / 255f);
            }
        }

        return Collections.singletonList(ts);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 3 || arguments.size() < 1) throw new InvalidArgumentException("threshold: number of arguments is " + arguments.size() + ". Must be one to three.");
        if (!(arguments.get(0) instanceof Double)) throw new InvalidArgumentException("threshold: argument is " + arguments.get(0).getClass().getName() + ". Must be a number");

        if (arguments.size() > 1 && !(arguments.get(1) instanceof String)) throw new InvalidArgumentException("color: argument is " + arguments.get(1).getClass().getName() + ". Must be a string");
        if (arguments.size() > 2 && !(arguments.get(2) instanceof String)) throw new InvalidArgumentException("color: argument is " + arguments.get(2).getClass().getName() + ". Must be a string");

    }
}
