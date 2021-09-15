package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.Target;

import java.util.ArrayList;
import java.util.Objects;
import java.util.OptionalDouble;


/**
 * @author Andrei Ivanov
 */
public class MovingMaxFunction extends MovingFunction {

    public MovingMaxFunction(String text) {
        super(text, "movingMax");
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 2) throw new InvalidArgumentException("movingAverage: number of arguments is " + arguments.size() + ". Must be two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("movingAverage: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double) && !(arguments.get(1) instanceof String)) throw new InvalidArgumentException("movingAverage: argument is " + arguments.get(1).getClass().getName() + ". Must be a number or a string");
    }

    /**
     * Desc :
     *   return max value from array list
     * @param movingMetrics : list array for moving_metrics
     * @return max value
     */
    @Override
    public Double operation(ArrayList<Double> movingMetrics) {
        OptionalDouble optionalDouble = movingMetrics.stream().filter(Objects:: nonNull).mapToDouble(Double::doubleValue).max();
        return optionalDouble.isPresent() ? optionalDouble.getAsDouble() : null;
    }
}
