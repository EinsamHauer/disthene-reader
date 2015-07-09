package net.iponweb.disthene.reader.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class CollectionUtils {

    public static Double average(Collection<Double> values) {
        List<Double> filteredValues = filterNulls(values);
        if (filteredValues.size() == 0) return null;

        double sum = 0;
        for(Double value : filteredValues) {
                sum += value;
        }

        return sum / filteredValues.size();
    }

    public static Double median(Collection<Double> values) {
        List<Double> filteredValues = filterNulls(values);

        if (filteredValues.size() == 0) return 0.;
        if (filteredValues.size() == 1) return filteredValues.get(0);

        Collections.sort(filteredValues);

        if (filteredValues.size() % 2 == 0) {
            return (filteredValues.get(filteredValues.size() / 2) + filteredValues.get(filteredValues.size() / 2 - 1)) / 2.;
        } else {
            return filteredValues.get(filteredValues.size() / 2);
        }
    }

    public static Double sum(Collection<Double> values) {
        List<Double> filteredValues = filterNulls(values);
        double sum = 0;
        for(Double value : filteredValues) {
            sum += value;
        }
        return sum;
    }

    public static Double stdev(Collection<Double> values) {
        List<Double> filteredValues = filterNulls(values);
        if (filteredValues.size() < 2) return null;

        Double average = average(filteredValues);
        if (average == null) return null;
        double variance = 0;

        for(Double value : filteredValues) {
            variance += (value - average) * (value - average);
        }

        return Math.sqrt(variance) / filteredValues.size();
    }

    public static Double percentile(Collection<Double> values, double percentile, boolean interpolate) {
        List<Double> filteredValues = filterNulls(values);
        Collections.sort(filteredValues);

        if (filteredValues.size() == 0) return null;

        double fractionalRank = (percentile / 100.0) * (filteredValues.size() + 1);
        int rank = (int) fractionalRank;
        double rankFraction = fractionalRank - rank;

        double result;

        if (!interpolate) {
            rank += (int) Math.ceil(rankFraction);
        }

        if (rank == 0) {
            result = filteredValues.get(0);
        } else if (rank == filteredValues.size() + 1) {
            result = filteredValues.get(filteredValues.size() - 1);
        } else {
            result = filteredValues.get(rank - 1);
        }

        if (interpolate && rank != filteredValues.size()) {
            result = result + rankFraction * (filteredValues.get(rank) - result);
        }

        return result;
    }

    public static Double last(Collection<Double> values) {
        List<Double> filteredValues = filterNulls(values);

        return filteredValues.size() > 0 ? filteredValues.get(filteredValues.size() - 1) : null;
    }

    public static Double max(Collection<Double> values) {
        List<Double> filteredValues = filterNulls(values);
        if (filteredValues.size() == 0) return null;

        return Collections.max(filteredValues);
    }

    public static Double min(Collection<Double> values) {
        List<Double> filteredValues = filterNulls(values);
        if (filteredValues.size() == 0) return null;

        return Collections.min(filteredValues);
    }

    private static List<Double> filterNulls(Collection<Double> values) {
        List<Double> result = new ArrayList<>();
        for(Double value : values) {
            if (value != null) result.add(value);
        }

        return result;
    }

}
