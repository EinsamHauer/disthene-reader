package net.iponweb.disthene.reader.graphite.functions;

import com.google.common.base.Joiner;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Andrei Ivanov
 */
public class AliasByNodeFunction extends DistheneFunction {

    public AliasByNodeFunction(String text) {
        super(text, "aliasByNode");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        return setNamesByNodes(processedArguments, getNodes(arguments, computeMinimumLength(processedArguments)));
    }

    private int computeMinimumLength(List<TimeSeries> timeSeriesList) {
        int minLength = Integer.MAX_VALUE;
        for (TimeSeries ts : timeSeriesList) {
            int length = TimeSeriesUtils.DOT_PATTERN.split(ts.getName()).length;
            if (length < minLength) {
                minLength = length;
            }
        }
        return minLength;
    }

    private int[] getNodes(List<Object> arguments, int minLength) {
        int[] nodes = new int[arguments.size() - 1];
        for (int i = 1; i < arguments.size(); i++) {
            int node = ((Double) arguments.get(i)).intValue();
            if (node < 0) {
                nodes[i - 1] = node + minLength;
            } else {
                nodes[i - 1] = node;
            }
        }
        return nodes;
    }

    private List<TimeSeries> setNamesByNodes(List<TimeSeries> timeSeriesList, int[] nodes) {
        for (TimeSeries ts : timeSeriesList) {
            String[] split = TimeSeriesUtils.DOT_PATTERN.split(ts.getName());
            List<String> parts = new ArrayList<>();
            for (int node : nodes) {
                parts.add(split[node]);
            }
            ts.setName(Joiner.on(".").join(parts));
        }
        return timeSeriesList;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        check(arguments.size() >= 2,
                "aliasByNode: number of arguments is " +
                        arguments.size() + ". Must be at least two.");

        Optional<Object> argSeries = Optional.ofNullable(arguments.get(0));
        check(argSeries.orElse(null) instanceof Target,
                "aliasByNode: First argument is " +
                        getClassName(argSeries.orElse(null)) + ". Must be series.");

        for (int i = 1; i < arguments.size(); i++) {
            Optional<Object> argNumber = Optional.ofNullable(arguments.get(i));
            check(argNumber.orElse(null) instanceof Double,
                    "aliasByNode: argument is " +
                            getClassName(argNumber.orElse(null)) + ". Must be a number.");
        }
    }
}
