package net.iponweb.disthene.reader.graphite;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;

import java.util.List;

/**
 * @author Andrei Ivanov
 */
public abstract class Target {

    private String text;

    public Target(String text) {
        this.text = text;
    }

    public abstract List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
