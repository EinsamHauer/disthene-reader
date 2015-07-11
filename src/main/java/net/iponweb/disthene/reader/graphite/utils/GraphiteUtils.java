package net.iponweb.disthene.reader.graphite.utils;

import net.iponweb.disthene.reader.graphite.utils.UnitSystem;

import java.math.BigDecimal;

/**
 * @author Andrei Ivanov
 */
public class GraphiteUtils {

    private static double THRESHOLD =  0.00000000001;

    public static double formatUnitValue(double value, double step, UnitSystem unitSystem) {
        // Firstly, round the value a bit
        if (value > 0 && value < 1.0) {
            value = new BigDecimal(value).setScale(2 - (int) Math.log10(value), BigDecimal.ROUND_HALF_DOWN).doubleValue();
        }


        for (Unit unit : unitSystem.getPrefixes()) {
            if (Math.abs(value) >= unit.getValue() && step >= unit.getValue()) {
                double v2 = value / unit.getValue();
                if (v2 - Math.floor(v2) < THRESHOLD && value > 1) {
                    v2 = Math.floor(v2);
                }
                return v2;
            }
        }

        if (value - Math.floor(value) < THRESHOLD && value > 1) {
            return Math.floor(value);
        }

        return value;
    }

    public static double formatUnitValue(double value, UnitSystem unitSystem) {
        // Firstly, round the value a bit
        if (value > 0 && value < 1.0) {
            value = new BigDecimal(value).setScale(2 - (int) Math.log10(value), BigDecimal.ROUND_HALF_DOWN).doubleValue();
        }


        for (Unit unit : unitSystem.getPrefixes()) {
            if (Math.abs(value) >= unit.getValue()) {
                double v2 = value / unit.getValue();
                if (v2 - Math.floor(v2) < THRESHOLD && value > 1) {
                    v2 = Math.floor(v2);
                }
                return v2;
            }
        }

        if (value - Math.floor(value) < THRESHOLD && value > 1) {
            return Math.floor(value);
        }

        return value;
    }

    public static String formatUnitPrefix(double value, double step, UnitSystem unitSystem) {
        for (Unit unit : unitSystem.getPrefixes()) {
            if (Math.abs(value) >= unit.getValue() && step >= unit.getValue()) {
                return unit.getPrefix();
            }

        }

        return "";
    }

    public static String formatUnitPrefix(double value, UnitSystem unitSystem) {
        for (Unit unit : unitSystem.getPrefixes()) {
            if (Math.abs(value) >= unit.getValue()) {
                return unit.getPrefix();
            }

        }

        return "";
    }

    public static String formatValue(double value, UnitSystem unitSystem) {
        return String.format("%.2f%s", formatUnitValue(value, unitSystem), formatUnitPrefix(value, unitSystem));
    }
}
