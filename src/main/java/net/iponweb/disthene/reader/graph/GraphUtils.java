package net.iponweb.disthene.reader.graph;

import net.iponweb.disthene.reader.beans.TimeSeries;

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
}
