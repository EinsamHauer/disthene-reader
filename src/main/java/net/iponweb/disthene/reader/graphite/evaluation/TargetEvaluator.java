package net.iponweb.disthene.reader.graphite.evaluation;

import com.google.common.collect.ObjectArrays;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.config.Rollup;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidNumberOfSeriesException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.exceptions.TooMuchDataExpectedException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.functions.DistheneFunction;
import net.iponweb.disthene.reader.service.metric.MetricService;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Andrei Ivanov
 */
public class TargetEvaluator {
    final static Logger logger = Logger.getLogger(TargetEvaluator.class);

    private MetricService metricService;

    public TargetEvaluator(MetricService metricService) {
        this.metricService = metricService;
    }

    public List<TimeSeries> eval(Target target) throws EvaluationException {
        return target.evaluate(this);
    }

    public List<TimeSeries> visit(PathTarget pathTarget) throws EvaluationException {
        try {
            return metricService.getMetricsAsList(pathTarget.getTenant(), Collections.singletonList(pathTarget.getPath()), pathTarget.getFrom(), pathTarget.getTo());
        } catch (ExecutionException | InterruptedException | TooMuchDataExpectedException e) {
            logger.error(e.getMessage());
            logger.debug(e);
            throw new EvaluationException(e);
        }
    }

    public List<TimeSeries> visit(DistheneFunction function) throws EvaluationException {
        return function.evaluate(this);
    }

    //todo: the logic below is duplicated several times - fix it!
    public TimeSeries getEmptyTimeSeries(long from, long to) {
        Long now = System.currentTimeMillis() * 1000;
        Long effectiveTo = Math.min(to, now);
        Rollup bestRollup = metricService.getRollup(from);
        Long effectiveFrom = (from % bestRollup.getRollup()) == 0 ? from : from + bestRollup.getRollup() - (from % bestRollup.getRollup());
        effectiveTo = effectiveTo - (effectiveTo % bestRollup.getRollup());

        int length = (int) ((effectiveTo - effectiveFrom) / bestRollup.getRollup() + 1);

        TimeSeries ts = new TimeSeries("", from, to, bestRollup.getRollup());
        ts.setValues(new Double[length]);

        return ts;
    }

    // todo: suboptimal
    public List<TimeSeries> bootstrap(Target target, List<TimeSeries> original, long period) throws EvaluationException {
        if (original.size() == 0) return new ArrayList<>();

        List<TimeSeries> bootstrapped = new ArrayList<>();
        bootstrapped.addAll(eval(target.previous(period)));

        if (bootstrapped.size() != original.size()) throw new InvalidNumberOfSeriesException();
        if (!TimeSeriesUtils.checkAlignment(bootstrapped)) throw new TimeSeriesNotAlignedException();

        int step = original.get(0).getStep();

        // normalize (assuming bootstrapped step can only be bigger
        if (bootstrapped.get(0).getStep() != step) {
            int ratio = bootstrapped.get(0).getStep() / step;
            for(TimeSeries ts : bootstrapped) {
                List<Double> values = new ArrayList<>();
                for (int i = 0; i < ts.getValues().length; i++) {
                    values.addAll(Collections.nCopies(ratio, ts.getValues()[i]));
                }
                ts.setValues(values.toArray(new Double[values.size()]));
            }
        }

        for (int i = 0; i < bootstrapped.size(); i++) {
            bootstrapped.get(i).setValues(ObjectArrays.concat(bootstrapped.get(i).getValues(), original.get(i).getValues(), Double.class));
            bootstrapped.get(i).setStep(step);
        }

        return bootstrapped;
    }
}
