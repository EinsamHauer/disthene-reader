package net.iponweb.disthene.reader.utils;

import net.iponweb.disthene.reader.beans.TimeSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class TimeSeriesUtils {


    public static boolean checkAlignment(List<TimeSeries> timeSeries) {
        if (timeSeries.size() == 0) return true;

        long from = timeSeries.get(0).getFrom();
        long to = timeSeries.get(0).getTo();
        long step = timeSeries.get(0).getStep();
        int length = timeSeries.get(0).getValues().length;

        boolean result = true;

        for(TimeSeries ts : timeSeries) {
            if (ts.getFrom() != from || ts.getTo() != to || ts.getStep() != step || ts.getValues().length != length) result = false;
        }

        if (!result) {
            align(timeSeries);
        }
        return true;
    }

    private static void align(List<TimeSeries> timeSeries) {
        //todo: we assume that steps are multiples of each other. So, basically, we are selecting the largest step. Not sure if this needs be fixed
        //todo: we also assume that time series should start and end at even steps
        //todo: one more assumption: we are taking an intersection of resulting ranges
        int step = Integer.MIN_VALUE;

        for (TimeSeries ts : timeSeries) {
            step = Math.max(step, ts.getStep());
        }

        long from = Long.MIN_VALUE;
        long to = Long.MAX_VALUE;

        for (TimeSeries ts : timeSeries) {
            long potentialFrom;
            if (ts.getFrom() % step == 0) {
                potentialFrom = ts.getFrom();
            } else {
                potentialFrom = ts.getFrom() - ts.getFrom() % step + step;
            }
            from = Math.max(from, potentialFrom);

            long potentialTo;
            if (ts.getTo() % step == 0) {
                potentialTo = ts.getTo();
            } else {
                potentialTo = ts.getTo() - ts.getTo() % step;
            }
            to = Math.min(to, potentialTo);

        }

        // we have a new step, let's consolidate each time series one by one
        for (TimeSeries ts : timeSeries) {
            consolidate(ts, step, from, to);
        }
    }

    // todo: pretty awkward - rework?
    // todo: think about different consolidation functions?
    // todo: assuming timeSeries.from <= from <= to <= timeSeries.to - has to be checked?
    // todo: from % step == 0 && to % step == 0
    private static void consolidate(TimeSeries timeSeries, int step, long from, long to) {
        List<Double> consolidated = new ArrayList<>();
        List<Double> buffer = new ArrayList<>();

        int index = 0;
        while (timeSeries.getFrom() + index * timeSeries.getStep() <= to) {
            buffer.add(timeSeries.getValues()[index]);

            if ((timeSeries.getFrom() + index * timeSeries.getStep()) % step == 0) {
                consolidated.add(CollectionUtils.average(buffer));
                buffer.clear();
            }

            index++;
        }

        timeSeries.setFrom(from);
        timeSeries.setTo(to);
        timeSeries.setStep(step);
        timeSeries.setValues(consolidated.toArray(new Double[1]));
    }
}
