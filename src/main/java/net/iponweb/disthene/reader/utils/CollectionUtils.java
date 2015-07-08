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
        double sum = 0;
        double count = 0;
        for(Double value : values) {
            if (value != null) {
                sum += value;
                count++;
            }
        }

        return count != 0 ?  sum / count : 0;
    }

    public static Double median(Collection<Double> values) {
        List<Double> filteredValues = new ArrayList<>();
        for(Double value : values) {
            if (value != null) filteredValues.add(value);
        }

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
        double sum = 0;
        for(Double value : values) {
            if (value != null) {
                sum += value;
            }
        }

        return sum;
    }

    public static Double stdev(Collection<Double> values) {
        List<Double> filteredValues = new ArrayList<>();
        for(Double value : values) {
            if (value != null) filteredValues.add(value);
        }

        if (filteredValues.size() < 2) return 0.;

        double average = average(filteredValues);
        double variance = 0;

        for(Double value : filteredValues) {
            variance += (value - average) * (value - average);
        }

        return Math.sqrt(variance) / filteredValues.size();
    }

}
