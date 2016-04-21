package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.CollectionUtils;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class AggregateLineFunction extends DistheneFunction {

    public AggregateLineFunction(String text) {
        super(text, "aggregateLine");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        String aggregation = arguments.size() > 1 ? ((String) arguments.get(1)).toLowerCase().replaceAll("[\"\']", "") : "avg";

        for (TimeSeries ts : processedArguments) {
            List<Double> valuesArray = Arrays.asList(ts.getValues());
            switch (aggregation) {
                case "last": {
                    Double v = CollectionUtils.last(valuesArray);
                    if (v != null) CollectionUtils.constant(ts.getValues(), v);
                    break;
                }
                case "avg": {
                    Double v = CollectionUtils.average(valuesArray);
                    if (v != null) CollectionUtils.constant(ts.getValues(), v);
                    break;
                }
                case "total": {
                    Double v = CollectionUtils.sum(valuesArray);
                    if (v != null) CollectionUtils.constant(ts.getValues(), v);
                    break;
                }
                case "min": {
                    Double v = CollectionUtils.min(valuesArray);
                    if (v != null) CollectionUtils.constant(ts.getValues(), v);
                    break;
                }
                case "max": {
                    Double v = CollectionUtils.max(valuesArray);
                    if (v != null) CollectionUtils.constant(ts.getValues(), v);
                    break;
                }
                case "first": {
                    Double v = CollectionUtils.first(valuesArray);
                    if (v != null) CollectionUtils.constant(ts.getValues(), v);
                    break;
                }
            }
            setResultingName(ts);
        }

        return processedArguments;
    }


    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 1 || arguments.size() > 2) throw new InvalidArgumentException("aggregateLine: number of arguments is " + arguments.size() + ". Must be one or two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("aggregateLine: argument is " + arguments.get(0).getClass().getName() + ". Must be series");

        if (arguments.size() > 1) {
            if (!(arguments.get(1) instanceof String)) throw new InvalidArgumentException("aggregateLine: argument is " + arguments.get(1).getClass().getName() + ". Must be a string");
            String argument = ((String) arguments.get(1)).toLowerCase().replaceAll("[\"\']", "");
            if (!argument.equals("last") && !argument.equals("avg") && !argument.equals("total") && !argument.equals("min") && !argument.equals("max") && !argument.equals("first")) {
                throw new InvalidArgumentException("aggregateLine: must be aggregation.");
            }
        }
    }
}
