package net.iponweb.disthene.reader.graphite.utils;

import com.google.common.math.DoubleMath;

import java.math.BigDecimal;

/**
 * @author Andrei Ivanov
 */
public class GraphiteUtils {

    private static double THRESHOLD =  0.00000000001;

    public static double formatUnitValue(double value, double step, UnitSystem unitSystem) {
        value = magicRound(value);


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
/*
        // Firstly, round the value a bit
        if (value > 0 && value < 1.0) {
            BigDecimal bd = BigDecimal.valueOf(value);
            value = bd.setScale(bd.scale() - 1, BigDecimal.ROUND_HALF_DOWN).doubleValue();
        }

*/

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

        return  value;
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

    public static String formatDoubleSpecialPlain(Double value) {
        BigDecimal bigDecimal = BigDecimal.valueOf(value);

        // do not do this for math integers
        if (!DoubleMath.isMathematicalInteger(value)) {
            // precision is just like in graphite (scale check redundant but let it be)
            if (bigDecimal.precision() > 12 && bigDecimal.scale() > 0) {
                int roundTo = bigDecimal.scale() - bigDecimal.precision() + 12 > 0 ? bigDecimal.scale() - bigDecimal.precision() + 12 : 0;
                bigDecimal = bigDecimal.setScale(roundTo, BigDecimal.ROUND_HALF_UP);
            }
        }


        return bigDecimal.stripTrailingZeros().toPlainString();
    }

    // todo: this "magic rounding" is a complete atrocity - fix it!
    public static double magicRound(double value) {
        if (value > -1.0 && value < 1.0) {
            return new BigDecimal(value).setScale(2 - (int) Math.log10(Math.abs(value)), BigDecimal.ROUND_HALF_UP).doubleValue();
        } else {
            return value;
        }
    }

    private final static BigDecimal ONE = BigDecimal.valueOf(1.0);
    private final static BigDecimal MINUS_ONE = BigDecimal.valueOf(-1.0);
    public static BigDecimal magicRound(BigDecimal value) {
        if (value.compareTo(MINUS_ONE) > 0 && value.compareTo(ONE) < 0) {
            return value.setScale(2 - (int) Math.log10(Math.abs(value.doubleValue())), BigDecimal.ROUND_HALF_UP);
        } else {
            return value;
        }
    }
}
