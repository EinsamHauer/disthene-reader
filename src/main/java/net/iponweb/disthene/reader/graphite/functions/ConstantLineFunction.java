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
public class ConstantLineFunction extends DistheneFunction {

    public ConstantLineFunction(String text) {
        super(text, "constantLine");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        Double constant = (Double) arguments.get(0);

        TimeSeries ts = evaluator.getEmptyTimeSeries(from, to);
        CollectionUtils.constant(ts.getValues(), constant);

        ts.setName(String.valueOf(constant));

        return Collections.singletonList(ts);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 1) throw new InvalidArgumentException("constantLine: number of arguments is " + arguments.size() + ". Must be one.");
        if (!(arguments.get(0) instanceof Double)) throw new InvalidArgumentException("constantLine: argument is " + arguments.get(0).getClass().getName() + ". Must be a number");
    }
}
