package net.iponweb.disthene.reader.graphite.functions.registry;

import javafx.scene.transform.Scale;
import net.iponweb.disthene.reader.exceptions.InvalidFunctionException;
import net.iponweb.disthene.reader.graphite.functions.*;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrei Ivanov
 */
public class FunctionRegistry {

    private static final Map<String, Class<? extends DistheneFunction>> registry = new HashMap<>();

    static {
        registry.put("absolute", AbsoluteFunction.class);
        registry.put("averageSeries", AverageSeriesFunction.class);
        registry.put("avg", AverageSeriesFunction.class);
        registry.put("countSeries", CountSeriesFunction.class);
        registry.put("dashed", DashedFunction.class);
        registry.put("diffSeries", DiffSeriesFunction.class);
        registry.put("divideSeries", DivideSeriesFunction.class);
        registry.put("integral", IntegralFunction.class);
        registry.put("logarithm", LogarithmFunction.class);
        registry.put("log", LogarithmFunction.class);
        registry.put("maxSeries", MaxSeriesFunction.class);
        registry.put("minSeries", MinSeriesFunction.class);
        registry.put("multiplySeries", MultiplySeriesFunction.class);
        registry.put("offset", OffsetFunction.class);
        registry.put("offsetToZero", OffsetToZeroFunction.class);
        registry.put("rangeOfSeries", RangeOfSeriesFunction.class);
        registry.put("scale", ScaleFunction.class);
        registry.put("scaleToSeconds", ScaleToSecondsFunction.class);
        registry.put("secondYAxis", SecondYAxisFunction.class);
        registry.put("stacked", StackedFunction.class);
        registry.put("sumSeries", SumSeriesFunction.class);
        registry.put("sum", SumSeriesFunction.class);
    }

    public static DistheneFunction getFunction(String name) throws InvalidFunctionException {
        if (registry.get(name) == null) {
            throw new InvalidFunctionException();
        }

        try {
            Constructor<DistheneFunction> constructor = (Constructor<DistheneFunction>) registry.get(name).getConstructor(String.class);
            return constructor.newInstance(name);
        } catch (Exception e) {
            throw new InvalidFunctionException();
        }
    }
}
