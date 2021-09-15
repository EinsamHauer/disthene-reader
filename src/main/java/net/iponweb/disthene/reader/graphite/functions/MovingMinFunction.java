package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.Target;

import java.util.*;

/**
 * @author Andrei Ivanov
 */
public class MovingMinFunction extends MovingFunction {

    public MovingMinFunction(String text) {
        super(text, "movingMin");
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 2) throw new InvalidArgumentException("movingAverage: number of arguments is " + arguments.size() + ". Must be two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("movingAverage: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!(arguments.get(1) instanceof Double) && !(arguments.get(1) instanceof String)) throw new InvalidArgumentException("movingAverage: argument is " + arguments.get(1).getClass().getName() + ". Must be a number or a string");
    }
    
    /**
     * Desc :
     *   return min value from array list
     * @param movingMetrics : list array for moving_metrics
     * @return minValue typed double
     */
    @Override
    public Double operation(ArrayList <Double> movingMetrics) {
        OptionalDouble optionalDouble = movingMetrics.stream().filter(Objects :: nonNull).mapToDouble(Double::doubleValue).min();
        return optionalDouble.isPresent() ? optionalDouble.getAsDouble() : null;
    }
}
