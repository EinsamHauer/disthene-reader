package net.iponweb.disthene.reader.graphite.evaluation;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.functions.DistheneFunction;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.metric.MetricService;

import java.util.List;

/** @author Andrei Ivanov */
public abstract class TargetEvaluator {

    private MetricService metricService;
    private IndexService indexService;

    abstract public List<TimeSeries> eval(Target target) throws EvaluationException;

    abstract public List<TimeSeries> visit(PathTarget pathTarget) throws EvaluationException;

    abstract public List<TimeSeries> visit(DistheneFunction function) throws EvaluationException;

    abstract public TimeSeries getEmptyTimeSeries(long from, long to);

    abstract public List<TimeSeries> bootstrap(Target target, List<TimeSeries> original, long period) throws EvaluationException;
}
