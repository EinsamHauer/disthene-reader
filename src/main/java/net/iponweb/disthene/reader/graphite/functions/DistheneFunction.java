package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.InvalidFunctionException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.functions.registry.FunctionRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public abstract class DistheneFunction extends Target {

    protected List<Object> arguments = new ArrayList<>();
    protected String name;
    protected Long from;
    protected Long to;

    public DistheneFunction(String text, String name) {
        super(text);
        this.name = name;
    }

    public void addArg(Object argument) {
        arguments.add(argument);
    }

    public abstract void checkArguments() throws  InvalidArgumentException;

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                ", arguments=" + arguments +
                '}';
    }

    public String getName() {
        return name;
    }

    protected void setResultingName(TimeSeries timeSeries) {
        timeSeries.setName(name + "(" + timeSeries.getName() + ")");
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    protected String getResultingName(TimeSeries timeSeries) {
        return name + "(" + timeSeries.getName() + ")";
    }

    @Override
    public Target shiftBy(long shift) {
        try {
            DistheneFunction function = FunctionRegistry.getFunction(getContext(), name, from - shift, to - shift);

            for (Object argument : arguments) {
                if (argument instanceof Target) {
                    function.addArg(((Target) argument).shiftBy(shift));
                } else {
                    function.addArg(argument);
                }
            }

            // we'd like to keep the text intact vs replacing it with function name here
            function.setText(getText());
            return function;
        } catch (InvalidFunctionException ignored) {
        }

        return null;
    }

    @Override
    public Target previous(long period) {
        try {
            DistheneFunction function = FunctionRegistry.getFunction(getContext(), name, from - period , from - 1);

            for (Object argument : arguments) {
                if (argument instanceof Target) {
                    function.addArg(((Target) argument).previous(period));
                } else {
                    function.addArg(argument);
                }
            }

            return function;
        } catch (InvalidFunctionException ignored) {
        }

        return null;
    }
}
