package net.iponweb.disthene.reader.graphite;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;

import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class PathTarget extends Target {

    private String path;
    private String tenant;
    private Long from;
    private Long to;

    public PathTarget(String text, String path, String tenant, Long from, Long to) {
        super(text);
        this.path = path;
        this.tenant = tenant;
        this.from = from;
        this.to = to;
    }

    public String getPath() {
        return path;
    }

    public String getTenant() {
        return tenant;
    }

    public Long getFrom() {
        return from;
    }

    public Long getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "PathTarget{" +
                "path='" + path + '\'' +
                '}';
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        return evaluator.visit(this);
    }
}
