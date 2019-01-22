package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.beans.TimeSeriesOption;
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
public class HoltWintersConfidenceAreaFunction extends DistheneFunction {

    public HoltWintersConfidenceAreaFunction(String text) {
        super(text, "holtWintersConfidenceArea");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        double delta = arguments.size() > 1 ? (Double) arguments.get(1) : 3;

        HoltWinters holtWinters = HoltWinters.analyze((Target) arguments.get(0), evaluator);
        List<TimeSeries> forecasts = holtWinters.getForecasts();
        List<Double> deviations = holtWinters.getDeviations();

        if (forecasts.size() == 0) return Collections.emptyList();

        long from = forecasts.get(0).getFrom();
        long to = forecasts.get(0).getTo();
        int step = forecasts.get(0).getStep();
        int length = forecasts.get(0).getValues().length;

        List<TimeSeries> result = new ArrayList<>();

        for (int i = 0; i < forecasts.size(); i++) {
            TimeSeries upper = new TimeSeries(forecasts.get(i).getName(), from, to, step);
            TimeSeries lower = new TimeSeries(forecasts.get(i).getName(), from, to, step);
            Double[] upperValues = new Double[length];
            Double[] lowerValues = new Double[length];

            Double[] forecastValues = forecasts.get(i).getValues();
            double deviation = deviations.get(i);

            for (int j = 0; j < length; j++) {
                if (forecastValues[j] != null) {
                    upperValues[j] = forecastValues[j] + delta * deviation;
                    lowerValues[j] = forecastValues[j] - delta * deviation;
                } else {
                    upperValues[j] = null;
                    lowerValues[j] = null;
                }
            }

            upper.setValues(upperValues);
            upper.setName("holtWintersConfidenceUpper(" + forecasts.get(i).getName() + ")");
            upper.addOption(TimeSeriesOption.STACKED);

            lower.setValues(lowerValues);
            lower.setName("holtWintersConfidenceLower(" + forecasts.get(i).getName() + ")");
            lower.addOption(TimeSeriesOption.STACKED);
            lower.addOption(TimeSeriesOption.INVISIBLE);

            result.add(lower);
            result.add(upper);
        }

        return result;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 2 || arguments.size() < 1) throw new InvalidArgumentException("holtWintersConfidenceArea: number of arguments is " + arguments.size() + ". Must be one or two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("holtWintersConfidenceArea: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (arguments.size() > 1 && !(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("holtWintersConfidenceArea: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");
    }
}
