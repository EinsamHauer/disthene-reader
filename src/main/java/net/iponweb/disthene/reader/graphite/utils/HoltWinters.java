package net.iponweb.disthene.reader.graphite.utils;

import com.google.common.collect.ObjectArrays;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidNumberOfSeriesException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.graphite.functions.DistheneFunction;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 *
 * This implementation is probably suboptimal.
 * Will probably be improved some day.
 */
public class HoltWinters {

    private static final long BOOTSTRAP_DELTA = 604800; // 7 days

    private Target target;
    private TargetEvaluator evaluator;

    private List<TimeSeries> predictions;
    private List<TimeSeries> deviations;

    public HoltWinters(Target target, TargetEvaluator evaluator) {
        this.target = target;
        this.evaluator = evaluator;
    }

    public static HoltWinters analyze(Target target, TargetEvaluator evaluator) throws EvaluationException {
        HoltWinters holtWinters = new HoltWinters(target, evaluator);
        holtWinters.analyze();
        return holtWinters;
    }

    private void analyze() throws EvaluationException {
        // Below assumes the results from evaluator will come in the same order
        // Firstly, let's get original series
        List<TimeSeries> original = new ArrayList<>();
        original.addAll(evaluator.eval(target));

        if (original.size() == 0) {
            predictions = new ArrayList<>();
            deviations = new ArrayList<>();
            return;
        }

        if (!TimeSeriesUtils.checkAlignment(original)) {
            throw new TimeSeriesNotAlignedException();
        }

        int length = original.get(0).getValues().length;
        int step = original.get(0).getStep();

        // Now, we need to bootstrap the series
        List<TimeSeries> bootstrapped = bootstrap(original, step);

        for (TimeSeries ts : bootstrapped) {
            analyzeSingleSeries(ts);
        }
    }

    private void analyzeSingleSeries(TimeSeries ts) {

    }

    private List<TimeSeries> bootstrap(List<TimeSeries> original, int step) throws EvaluationException {
        List<TimeSeries> bootstrapped = new ArrayList<>();
        bootstrapped.addAll(evaluator.eval(target.previous(BOOTSTRAP_DELTA)));

        if (bootstrapped.size() != original.size()) throw new InvalidNumberOfSeriesException();
        if (!TimeSeriesUtils.checkAlignment(bootstrapped)) {
            throw new TimeSeriesNotAlignedException();
        }

        // normalize
        if (bootstrapped.get(0).getStep() != step) {
            double ratio = bootstrapped.get(0).getStep() / step;
            for(TimeSeries ts : bootstrapped) {
                for (int i = 0; i < ts.getValues().length; i++) {
                    ts.getValues()[i] = ts.getValues()[i] * ratio;
                }
            }
        }

        for (int i = 0; i < bootstrapped.size(); i++) {
            bootstrapped.get(i).setValues(ObjectArrays.concat(bootstrapped.get(i).getValues(), original.get(i).getValues(), Double.class));
        }

        return bootstrapped;
    }

}
