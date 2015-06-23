package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public abstract class DistheneFunction extends Target {

    protected List<Object> arguments = new ArrayList<>();
    protected Class[] argumentTypes;

    public DistheneFunction(String text) {
        super(text);
    }

    public void addArg(Object argument) throws InvalidArgumentException {
        // check argument type
        if (!checkArgument(arguments.size(), argument)) {
            throw new InvalidArgumentException();
        }

        arguments.add(argument);
    }

    protected abstract boolean checkArgument(int position, Object argument);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                ", arguments=" + arguments +
                '}';
    }
}
