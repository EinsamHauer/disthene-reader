package net.iponweb.disthene.reader.utils;

import net.iponweb.disthene.reader.beans.TimeSeries;

import java.math.BigInteger;
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

        for(TimeSeries ts : timeSeries) {
            if (ts.getFrom() != from || ts.getTo() != to || ts.getStep() != step || ts.getValues().length != length) return false;
        }

        return true;
    }

    private static void align(List<TimeSeries> timeSeries) {
        //todo: we assume that steps are multiples of each other. So, basically, we are selecting the largest step. Not sure if this needs be fixed
        long step = timeSeries.get(0).getStep();

        for (TimeSeries ts : timeSeries) {
            step = step < ts.getStep() ? ts.getStep() : step;
        }

        // we have a new step, let's consolidate each time series one by one
        for (TimeSeries ts : timeSeries) {

        }


    }

    private static void consolidate(TimeSeries timeSeries, long step) {
        return;
    }

    private static long GCD(long a, long b) {
        return BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).longValue();
    }
}
