package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.graphite.utils.HoltWinters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class HoltWintersAberrationFunction extends DistheneFunction {

    public HoltWintersAberrationFunction(String text) {
        super(text, "holtWintersAberration");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        double delta = arguments.size() > 1 ? (Double) arguments.get(1) : 3;

        HoltWinters holtWinters = HoltWinters.analyze((Target) arguments.get(0), evaluator);
        List<TimeSeries> original = holtWinters.getOriginal();
        List<TimeSeries> forecasts = holtWinters.getForecasts();
        List<Double> deviations = holtWinters.getDeviations();

        if (forecasts.size() == 0) return Collections.emptyList();

        long from = forecasts.get(0).getFrom();
        long to = forecasts.get(0).getTo();
        int step = forecasts.get(0).getStep();
        int length = forecasts.get(0).getValues().length;

        List<TimeSeries> result = new ArrayList<>();

        for (int i = 0; i < forecasts.size(); i++) {
            TimeSeries aberration = new TimeSeries(forecasts.get(i).getName(), from, to, step);
            Double[] aberrationValues = new Double[length];

            Double[] originalValues = original.get(i).getValues();
            Double[] forecastValues = forecasts.get(i).getValues();
            double deviation = deviations.get(i);

            for (int j = 0; j < length; j++) {

                if (originalValues[j] == null) {
                    aberrationValues[j] = 0.;
                } else if (forecastValues[j] != null && originalValues[j] > forecastValues[j] + delta * deviation) {
                    aberrationValues[j] = originalValues[j] - forecastValues[j] - delta * deviation;
                } else if (forecastValues[j] != null && originalValues[j] < forecastValues[j] - delta * deviation) {
                    aberrationValues[j] = originalValues[j] - forecastValues[j] + delta * deviation;
                } else {
                    aberrationValues[j] = 0.;
                }
            }

            aberration.setValues(aberrationValues);
            setResultingName(aberration);
            result.add(aberration);
        }

        return result;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 2 || arguments.size() < 1) throw new InvalidArgumentException("holtWintersAberration: number of arguments is " + arguments.size() + ". Must be one or two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("holtWintersAberration: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (arguments.size() > 1 && !(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("holtWintersAberration: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");
    }
}
