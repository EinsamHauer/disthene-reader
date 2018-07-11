package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class IdentityFunction extends DistheneFunction {

    public IdentityFunction(String text) {
        super(text, "identity");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        TimeSeries ts = evaluator.getEmptyTimeSeries(from, to);
        int rawStep = (int) ((to - from) / ts.getValues().length);
        int step = (int) (Math.round(rawStep / 60.) * 60);

        for(int i = 0; i < ts.getValues().length; i++) {
            ts.getValues()[i] = (double) (from + i * step);
        }

        return Collections.singletonList(ts);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
//        if (arguments.size() != 0) throw new InvalidArgumentException("identity: number of arguments is " + arguments.size() + ". Must be zero.");
    }
}
