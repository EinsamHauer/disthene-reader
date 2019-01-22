package net.iponweb.disthene.reader.graph;

import java.util.List;

/**
 * @author Andrei Ivanov
 */
class GraphUtils {

    static Double safeMin(DecoratedTimeSeries ts) {
        double result = Double.POSITIVE_INFINITY;

        for (Double value : ts.getValues()) {
            if (value != null && !value.equals(Double.POSITIVE_INFINITY)) {
                result = value < result ? value : result;
            }
        }

        return result;
    }


    private static Double safeMax(DecoratedTimeSeries ts) {
        double result = Double.NEGATIVE_INFINITY;

        for (Double value : ts.getValues()) {
            if (value != null && !value.equals(Double.POSITIVE_INFINITY)) {
                result = value > result ? value : result;
            }
        }

        return result;
    }

    static Double safeMax(List<DecoratedTimeSeries> timeSeriesList) {
        double result = Double.NEGATIVE_INFINITY;

        for(DecoratedTimeSeries ts : timeSeriesList) {
            double single = safeMax(ts);
            if (single > result) result = single;
        }

        return result;
    }

    static double closest(double number, double[] neighbors) {
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
