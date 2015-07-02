package net.iponweb.disthene.reader.graph;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.beans.TimeSeriesOption;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class GraphUtils {

    public static Double safeMin(DecoratedTimeSeries ts) {
        double result = Double.POSITIVE_INFINITY;

        for (Double value : ts.getValues()) {
            if (value != null && !value.equals(Double.POSITIVE_INFINITY)) {
                result = value < result ? value : result;
            }
        }

        return result;
    }


    public static Double safeMax(DecoratedTimeSeries ts) {
        double result = Double.NEGATIVE_INFINITY;

        for (Double value : ts.getValues()) {
            if (value != null && !value.equals(Double.POSITIVE_INFINITY)) {
                result = value > result ? value : result;
            }
        }

        return result;
    }

    public static Double safeMax(List<DecoratedTimeSeries> timeSeriesList) {
        double result = Double.NEGATIVE_INFINITY;

        for(DecoratedTimeSeries ts : timeSeriesList) {
            double single = safeMax(ts);
            if (single > result) result = single;
        }

        return result;
    }

    public static Double maxSum(List<DecoratedTimeSeries> timeSeriesList) {
        if (timeSeriesList.size() == 0) return 0.;

        int length = timeSeriesList.get(0).getValues().length;
        if (length == 0) return 0.;


        double[] sums = new double[length];

        for(int i = 0; i < length; i++) {
            for(DecoratedTimeSeries ts : timeSeriesList) {
                if (!ts.hasOption(TimeSeriesOption.DRAW_AS_INFINITE)) {
                    sums[i] += (ts.getValues()[i] != null ? ts.getValues()[i] : 0);
                }
            }
        }

        double result = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < length; i++) {
            if (sums[i] > result) result = sums[i];
        }

        return result;
    }

    public static double closest(double number, double[] neighbors) {
        double distance = Double.POSITIVE_INFINITY;
        double closestNeighbor = neighbors[0];

        for(double neighbor : neighbors) {
            double d = Math.abs(neighbor - number);
            if (d < distance) {
                distance = d;
                closestNeighbor = neighbor;
            }
        }

        return closestNeighbor;
    }

}
