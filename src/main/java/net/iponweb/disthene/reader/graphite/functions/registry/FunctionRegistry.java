package net.iponweb.disthene.reader.graphite.functions.registry;

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
        registry.put("dashed", DashedFunction.class);
        registry.put("diffSeries", DiffSeriesFunction.class);
        registry.put("divideSeries", DivideSeriesFunction.class);
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
