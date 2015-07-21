package net.iponweb.disthene.reader.graphite.utils;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Andrei Ivanov
 *
 * This implementation is probably suboptimal.
 * Will probably be improved some day.
 */
public class HoltWinters {

    private static final long SEASON = 604800; // 7 days
    private static final long BOOTSTRAP = SEASON * 2;
    private static final double ALPHA = 0.2;
    private static final double GAMMA = 0.2;
    private static final double BETA = 0.0035;

    private Target target;
    private TargetEvaluator evaluator;

    List<TimeSeries> original = new ArrayList<>();
    private List<TimeSeries> forecasts = new ArrayList<>();
    private List<Double> deviations = new ArrayList<>();

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

    /**
     * Based on https://www.otexts.org/fpp/7/5
     * (Forecasting: principles and practice by Rob J Hyndman, George Athanasopoulos
     */
    private void analyzeSingleSeries(TimeSeries ts, long originalFrom, long originalTo, int originalLength) {
        int seasonLength = (int) (SEASON / ts.getStep());

        Double[] values = ts.getValues();
        Double[] forecast = new Double[values.length];


        int knownLength = ts.getValues().length - originalLength;

        // initialize
        List<Double> seasonal = new LinkedList<>();
        double baseline = values[1] != null ? values[1] : 0;
        double slope = baseline - (values[0] != null ? values[0] : 0);

        for (int i = 0; i < seasonLength; i++) {
            seasonal.add(values[i] != null ? values[i] : 0);
            forecast[i] = values[i] != null ? values[i] : 0;
        }

        for (int i = seasonLength; i < knownLength; i++) {
            forecast[i] = baseline + slope + seasonal.get(seasonLength - 1);

            double value = values[i] != null ? values[i] : 0;
            double previousBaseline = baseline;
            double previousSlope = slope;
            double previousSeasonal = seasonal.remove(0);

            baseline = ALPHA * (value - previousSeasonal) + (1.0 - ALPHA) * (previousBaseline + previousSlope);
            slope = BETA * (baseline - previousBaseline) + (1.0 - BETA) * previousSlope;
            seasonal.add(GAMMA * (value - baseline) + (1.0 - GAMMA) * previousSeasonal);
        }

        for (int i = knownLength; i < values.length; i++) {
            forecast[i] = baseline + slope + seasonal.get((seasonLength - 1 + (i - knownLength) % seasonLength) % seasonLength);
        }

        double sum = 0;
        for (int i = seasonLength; i < knownLength; i++) {
            double value = values[i] != null ? values[i] : 0;
            sum += (forecast[i] - value) * (forecast[i] - value);
        }

        double deviation = Math.sqrt(sum / (knownLength - seasonLength));

        TimeSeries forecastTimeSeries = new TimeSeries(ts.getName(), ts.getFrom(), ts.getTo(), ts.getStep());
        forecastTimeSeries.setValues(forecast);
        forecasts.add(revert(forecastTimeSeries, originalFrom, originalTo, originalLength));

        deviations.add(deviation);
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

    public List<Double> getDeviations() {
        return deviations;
    }

    public List<TimeSeries> getOriginal() {
        return original;
    }
}
