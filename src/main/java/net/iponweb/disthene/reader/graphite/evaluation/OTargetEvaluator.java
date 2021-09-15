package net.iponweb.disthene.reader.graphite.evaluation;

import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidNumberOfSeriesException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.exceptions.TooMuchDataExpectedException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.functions.DistheneFunction;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.metric.MetricService;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/** @author Andrei Ivanov */
public class OTargetEvaluator extends TargetEvaluator{

    private final MetricService metricService;
    private final IndexService indexService;

    public OTargetEvaluator(MetricService metricService, IndexService indexService) {
        this.metricService = metricService;
        this.indexService = indexService;
    }

    @Override
    public List<TimeSeries> eval(Target target) throws EvaluationException {
        return target.evaluate(this);
    }
    
    @Override
    public List<TimeSeries> visit(PathTarget pathTarget) throws EvaluationException {
        try {
            Set<String> paths =
                    (Set<String>) indexService.getPaths(pathTarget.getTenant(), Lists.newArrayList(pathTarget.getPath()));

            return metricService.getMetricsAsList(
                    pathTarget.getTenant(),
                    Lists.newArrayList(paths),
                    pathTarget.getFrom(),
                    pathTarget.getTo());
        } catch (ExecutionException | InterruptedException | TooMuchDataExpectedException e) {
            throw new EvaluationException(e);
        }
    }
    
    @Override
    public List<TimeSeries> visit(DistheneFunction function) throws EvaluationException {
        return function.evaluate(this);
    }
    
    @Override
    public TimeSeries getEmptyTimeSeries(long from, long to) {
        return null;
    }
    
    @Override
    public List<TimeSeries> bootstrap(Target target, List<TimeSeries> original, long period) throws EvaluationException {
        if (original.size() == 0) return new ArrayList<>();

        List<TimeSeries> bootstrapped = new ArrayList<>(eval(target.previous(period)));
        
        if (bootstrapped.size() != original.size()) throw new InvalidNumberOfSeriesException();
        if (!TimeSeriesUtils.checkAlignment(bootstrapped)) throw new TimeSeriesNotAlignedException();
        
        int step = original.get(0).getStep();
        
        // normalize (assuming bootstrapped step can only be bigger
        if (bootstrapped.get(0).getStep() != step) {
            int ratio = bootstrapped.get(0).getStep() / step;
            for (TimeSeries ts : bootstrapped) {
                List<Double> values = new ArrayList<>();
                for (int i = 0; i < ts.getValues().length; i++) {
                    values.addAll(Collections.nCopies(ratio, ts.getValues()[i]));
                }
                ts.setValues(values.toArray(new Double[0]));
            }
        }
        
        for (int i = 0; i < bootstrapped.size(); i++) {
            bootstrapped
                .get(i)
                .setValues(
                    ObjectArrays.concat(
                        bootstrapped.get(i).getValues(), original.get(i).getValues(), Double.class));
            bootstrapped.get(i).setStep(step);
        }
        
        return bootstrapped;
    }
}
