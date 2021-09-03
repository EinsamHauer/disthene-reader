package net.iponweb.disthene.reader.graphite.utils;

import com.google.common.math.DoubleMath;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Andrei Ivanov
 */
public class HumanValueFormatter extends ValueFormatter {

    private final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    public HumanValueFormatter() {
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        formatter.setDecimalFormatSymbols(symbols);
    }

    @Override
    protected String formatDoubleSpecialSmart(Double value) {
        BigDecimal bigDecimal = BigDecimal.valueOf(value);

        // do not do this for math integers
        if (!DoubleMath.isMathematicalInteger(value)) {
            // precision is just like in graphite (scale check redundant but let it be)
            if (bigDecimal.precision() > 12 && bigDecimal.scale() > 0) {
                int roundTo = Math.max(bigDecimal.scale() - bigDecimal.precision() + 12, 0);
                bigDecimal = bigDecimal.setScale(roundTo, RoundingMode.HALF_UP);
            }
        }

        return formatter.format(bigDecimal.doubleValue());
    }
}
