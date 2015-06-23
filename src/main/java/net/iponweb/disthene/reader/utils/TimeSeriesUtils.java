package net.iponweb.disthene.reader.utils;

import net.iponweb.disthene.reader.beans.TimeSeries;

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
}
