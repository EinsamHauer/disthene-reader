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
public class IntegralFunction extends DistheneFunction {


    public IntegralFunction(String text) {
        super(text, "integral");
    }

    private List<TimeSeries> integral(List<TimeSeries> timeSeries) {
        List<TimeSeries> result = new ArrayList<>();

        timeSeries.forEach( ts -> {
            String newName = String.format("integral(%s)", ts.getName());
            List<Double> newValues = new ArrayList<>();
            double current = 0.0;
            for (Double value : ts.getValues()) {
                if (value == null) {
                    newValues.add(value);
                } else {
                    current += value;
                    newValues.add(current);
                }
            }

            //public TimeSeries(String name, Long from, Long to, int step) {
            result.add(new TimeSeries(
                    newName,
                    ts.getFrom(),
                    ts.getTo(),
                    ts.getStep(),
                    newValues.toArray(new Double[0])));
        });

        return result;
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        return integral(processedArguments);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 1) throw new InvalidArgumentException("integral: number of arguments is 0. Must be at least one.");

        for(Object argument : arguments) {
            if (!(argument instanceof Target)) throw new InvalidArgumentException("integral: argument is " + argument.getClass().getName() + ". Must be series");
        }
    }
}
