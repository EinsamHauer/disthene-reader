package net.iponweb.disthene.reader.graphite.functions.factory;

import net.iponweb.disthene.reader.exceptions.InvalidFunctionException;
import net.iponweb.disthene.reader.graphite.functions.AverageFunction;
import net.iponweb.disthene.reader.graphite.functions.DistheneFunction;
import net.iponweb.disthene.reader.graphite.functions.SumFunction;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrei Ivanov
 */
public class FunctionFactory {

    private static final Map<String, Class<? extends DistheneFunction>> registry = new HashMap<>();

    static {
        registry.put("sumSeries", SumFunction.class);
        registry.put("averageSeries", AverageFunction.class);
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
