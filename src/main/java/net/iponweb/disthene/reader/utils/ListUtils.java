package net.iponweb.disthene.reader.utils;

import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class ListUtils {

    public static Double average(List<Double> values) {
        double sum = 0;
        for(Double value : values) {
            sum += value;
        }

        return values.size() > 0 ?  sum / values.size() : 0;
    }

    public static Double sum(List<Double> values) {
        double sum = 0;
        for(Double value : values) {
            sum += value;
        }

        return sum;
    }
}
