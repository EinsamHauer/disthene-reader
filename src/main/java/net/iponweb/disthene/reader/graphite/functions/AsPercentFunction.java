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
public class AsPercentFunction extends DistheneFunction {

    public AsPercentFunction(String text) {
        super(text, "asPercent");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        int length = processedArguments.get(0).getValues().length;

        if (arguments.size() > 1) {
            double total = (Double) arguments.get(1);
            for (TimeSeries ts : processedArguments) {
                for (int i = 0; i < length; i++) {
                    if (ts.getValues()[i] != null ) {
                        if (total != 0) {
                            ts.getValues()[i] = (ts.getValues()[i] / total) * 100;
                        } else {
                            ts.getValues()[i] = null;
                        }
                    }
                }

                ts.setName("asPercent(" + ts.getName() + "," + total + ")");
            }
        } else {
            double[] total = new double[length];
            for (int i = 0; i < length; i++) {
                total[i] = 0;
                for (TimeSeries ts : processedArguments) {
                    if (ts.getValues()[i] != null) {
                        total[i] += ts.getValues()[i];
                    }
                }
            }

            for (TimeSeries ts : processedArguments) {
                for (int i = 0; i < length; i++) {
                    if (ts.getValues()[i] != null ) {
                        if (total[i] != 0) {
                            ts.getValues()[i] = (ts.getValues()[i] / total[i]) * 100;
                        } else {
                            ts.getValues()[i] = null;
                        }
                    }
                }

                setResultingName(ts);
            }
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 2 || arguments.size() == 0) throw new InvalidArgumentException("asPercent: number of arguments is " + arguments.size() + ". Must be 1.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("asPercent: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (arguments.size() > 1 && !(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("asPercent: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");
    }
}
