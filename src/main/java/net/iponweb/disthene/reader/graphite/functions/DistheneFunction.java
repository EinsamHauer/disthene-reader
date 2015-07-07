package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public abstract class DistheneFunction extends Target {

    protected List<Object> arguments = new ArrayList<>();
    protected String name;

    public DistheneFunction(String text, String name) {
        super(text);
        this.name = name;
    }

    public void addArg(Object argument) throws InvalidArgumentException {
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

    protected String getResultingName(TimeSeries timeSeries) {
        return name + "(" + timeSeries.getName() + ")";
    }
}
