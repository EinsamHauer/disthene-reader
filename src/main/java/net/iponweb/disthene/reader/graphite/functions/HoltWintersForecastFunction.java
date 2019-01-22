package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.graphite.utils.HoltWinters;

import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class HoltWintersForecastFunction extends DistheneFunction {

    public HoltWintersForecastFunction(String text) {
        super(text, "holtWintersForecast");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> forecasts = HoltWinters.analyze((Target) arguments.get(0), evaluator).getForecasts();

        for (TimeSeries ts : forecasts) {
            setResultingName(ts);
        }

        return forecasts;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 1 || arguments.size() == 0) throw new InvalidArgumentException("holtWintersForecast: number of arguments is " + arguments.size() + ". Must be 1.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("holtWintersForecast: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
    }
}
