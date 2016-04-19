package net.iponweb.disthene.reader.graphite.utils;

import net.iponweb.disthene.reader.format.Format;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrei Ivanov
 */
public abstract class ValueFormatter {

    public enum ValueFormatterType {
        HUMAN, MACHINE;
    }

    static private final Map<ValueFormatterType, ValueFormatter> instances = new HashMap<>();

    static {
        instances.put(ValueFormatterType.HUMAN, new HumanValueFormatter());
        instances.put(ValueFormatterType.MACHINE, new MachineValueFormatter());
    }

    public static ValueFormatter getInstance(ValueFormatterType type) {
        return instances.get(type);
    }

    public static ValueFormatter getInstance(Format format) {
        return getInstance(Format.PNG.equals(format) ? ValueFormatterType.HUMAN : ValueFormatterType.MACHINE);
    }


    public String formatValue(double value, UnitSystem unitSystem) {
        return String.format("%s%s", formatDoubleSpecialSmart(GraphiteUtils.formatUnitValue(value, unitSystem)), GraphiteUtils.formatUnitPrefix(value, unitSystem));
    }

    protected abstract String formatDoubleSpecialSmart(Double value);
}
