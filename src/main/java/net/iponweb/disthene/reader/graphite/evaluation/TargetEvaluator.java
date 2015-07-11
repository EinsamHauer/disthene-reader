package net.iponweb.disthene.reader.graphite.evaluation;

import com.google.gson.Gson;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.config.Rollup;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.functions.DistheneFunction;
import net.iponweb.disthene.reader.service.metric.MetricService;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
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

    public List<TimeSeries> visit(PathTarget pathTarget) {
        try {
            return metricService.getMetricsAsList(pathTarget.getTenant(), Collections.singletonList(pathTarget.getPath()), pathTarget.getFrom(), pathTarget.getTo());
        } catch (Exception e) {
            logger.error(e);
            return Collections.emptyList();
        }
    }

    public List<TimeSeries> visit(DistheneFunction function) throws EvaluationException {
        return function.evaluate(this);
    }

    //todo: the logic below is duplicated several times - fix it!
    public TimeSeries getEmptyTimeSeries(long from, long to) {
        Long now = System.currentTimeMillis() * 1000;
        Long effectiveTo = Math.min(to, now);
        Rollup bestRollup = metricService.getRollup(from, effectiveTo);
        Long effectiveFrom = (from % bestRollup.getRollup()) == 0 ? from : from + bestRollup.getRollup() - (from % bestRollup.getRollup());
        effectiveTo = effectiveTo - (effectiveTo % bestRollup.getRollup());

        int length = (int) ((effectiveTo - effectiveFrom) / bestRollup.getRollup() + 1);

        TimeSeries ts = new TimeSeries("", from, to, bestRollup.getRollup());
        ts.setValues(new Double[length]);

        return ts;
    }

}
