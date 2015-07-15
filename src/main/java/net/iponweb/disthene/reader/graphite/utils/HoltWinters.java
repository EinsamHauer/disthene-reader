package net.iponweb.disthene.reader.graphite.utils;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrei Ivanov
 *
 * This implementation is probably suboptimal.
 * Will probably be improved some day.
 */
public class HoltWinters {

    private static final long BOOTSTRAP = 604800; // 7 days
    private static final long SEASON = 86400; // 7 days
    private static final double ALPHA = 0.1;
    private static final double GAMMA = 0.1;
    private static final double BETA = 0.0035;

    private Target target;
    private TargetEvaluator evaluator;

    List<TimeSeries> original = new ArrayList<>();
    private List<TimeSeries> forecasts = new ArrayList<>();
    private List<TimeSeries> deviations = new ArrayList<>();

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
        original.addAll(evaluator.eval(target));

        if (original.size() == 0) return;

        if (!TimeSeriesUtils.checkAlignment(original)) {
            throw new TimeSeriesNotAlignedException();
        }

        long from = original.get(0).getFrom();
        long to = original.get(0).getTo();
        int length = original.get(0).getValues().length;

        List<TimeSeries> bootstrapped = evaluator.bootstrap(target, original, BOOTSTRAP);

        for (TimeSeries ts : bootstrapped) {
            analyzeSingleSeries(ts, from, to, length);
        }
    }

    //todo: result differs from graphite too much
    /**
     * Based on https://www.otexts.org/fpp/7/5
     * (Forecasting: principles and practice by Rob J Hyndman, George Athanasopoulos
     */
    private void analyzeSingleSeries(TimeSeries ts, long originalFrom, long originalTo, int originalLength) {
        int seasonLength = (int) (SEASON / ts.getStep());

        Double[] values = ts.getValues();

        Double[] level = new Double[values.length];
        Double[] trend = new Double[values.length];
        Double[] seasonal = new Double[values.length];
        Double[] forecast = new Double[values.length];
        Double[] deviation = new Double[values.length];

        // not sure what's best be done when there are missing values. Let's assume that the value stays as is and seed is 0.

        // initialize
        values[0] = values[0] != null ? values[0] : 0;
        level[0] = values[0];
        trend[0] = 0.;
        seasonal[0] = 0.;
        forecast[0] = values[0];
        deviation[0] = 0.;

        for (int i = 1; i < values.length; i++) {
            values[i] = values[i] != null ? values[i] : values[i - 1];
            level[i] = ALPHA * (values[i] - (i >= seasonLength ? seasonal[i - seasonLength] : 0)) + (1 - ALPHA) * (level[i - 1] + trend[i - 1]);
            trend[i] = BETA * (level[i] - level[i - 1]) + (1 - BETA) * trend[i-1];
            seasonal[i] = GAMMA * (values[i] - level[i - 1] - trend[i - 1]) + (1 - GAMMA) * (i >= seasonLength ? seasonal[i - seasonLength] : 0);
            forecast[i] = level[i - 1] + trend[i - 1] + (i - 1 >= seasonLength ? seasonal[i - 1 - seasonLength] : 0);
            deviation[i] = GAMMA * Math.abs(values[i] - forecast[i]) + (1 - GAMMA) * (i >= seasonLength ? deviation[i - seasonLength] : 0);
        }

        TimeSeries forecastTimeSeries = new TimeSeries(ts.getName(), ts.getFrom(), ts.getTo(), ts.getStep());
        forecastTimeSeries.setValues(forecast);
        forecasts.add(revert(forecastTimeSeries, originalFrom, originalTo, originalLength));

        TimeSeries deviationTimeSeries = new TimeSeries(ts.getName(), ts.getFrom(), ts.getTo(), ts.getStep());
        deviationTimeSeries.setValues(deviation);
        deviations.add(revert(deviationTimeSeries, originalFrom, originalTo, originalLength));
    }

    private TimeSeries revert(TimeSeries ts, long originalFrom, long originalTo, int originalLength) {
        ts.setFrom(originalFrom);
        ts.setTo(originalTo);
        ts.setValues(Arrays.copyOfRange(ts.getValues(), ts.getValues().length - originalLength, ts.getValues().length));
        return ts;
    }

    public List<TimeSeries> getForecasts() {
        return forecasts;
    }

    public List<TimeSeries> getDeviations() {
        return deviations;
    }

    public List<TimeSeries> getOriginal() {
        return original;
    }
}
