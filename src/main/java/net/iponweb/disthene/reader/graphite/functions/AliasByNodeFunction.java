package net.iponweb.disthene.reader.graphite.functions;

import com.google.common.base.Joiner;
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
public class AliasByNodeFunction extends DistheneFunction {

    public AliasByNodeFunction(String text) {
        super(text, "aliasByNode");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        int[] nodes = new int[arguments.size() - 1];
        for (int i = 1; i < arguments.size(); i++) {
            nodes[i - 1] = ((Double) arguments.get(i)).intValue();
        }

        for (TimeSeries ts : processedArguments) {
            String[] split = ts.getName().split("\\.");
            List<String> parts = new ArrayList<String>();
            for (int node : nodes) {
                if (node >= 0 && node < split.length) {
                    parts.add(split[node]);
                }
            }
            ts.setName(Joiner.on(".").join(parts));
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() < 2) throw new InvalidArgumentException("aliasByNode: number of arguments is " + arguments.size() + ". Must be at least two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("aliasByNode: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        for (int i = 1; i < arguments.size(); i++) {
            if (!(arguments.get(i) instanceof Double))
                throw new InvalidArgumentException("groupByNodes: argument " + i + " is " + arguments.get(i).getClass().getName() + ". Must be a number");
        }
    }
}
