package net.iponweb.disthene.reader.graphite.functions;

import com.google.common.base.Joiner;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.graphite.utils.Grouper;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.List;


public class GroupByNodeFunction extends DistheneFunction {

    public GroupByNodeFunction(String text) {
        super(text, "groupByNode");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        String callbackName = (String) arguments.get(2);

        int[] positions = {((Double) arguments.get(1)).intValue()};
        
        return new Grouper(processedArguments, callbackName).byNodesIndex(positions);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 3)
            throw new InvalidArgumentException("groupByNode: number of arguments is " + arguments.size() + ". Must be a least two.");

        if (!(arguments.get(0) instanceof PathTarget))
            throw new InvalidArgumentException("groupByNode: argument is " + arguments.get(0).getClass().getName() + ". Must be series");

        if (!(arguments.get(1) instanceof Double))
            throw new InvalidArgumentException("groupByNode: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");

        if (!(arguments.get(2) instanceof String))
            throw new InvalidArgumentException("groupByNode: argument is " + arguments.get(2).getClass().getName() + ". Must be a string");

        if (!(Grouper.hasAggregationMethod((String) arguments.get(2)))) {
            throw new InvalidArgumentException("groupByNode: argument is " + arguments.get(2) + ". Must be one of " +
                                               Joiner.on(", ").join(Grouper.getAvailableAggregationMethods()) + ".");
        }
    }
}