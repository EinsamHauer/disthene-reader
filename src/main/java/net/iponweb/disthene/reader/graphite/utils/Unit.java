package net.iponweb.disthene.reader.graphite.utils;

/**
 * @author Andrei Ivanov
 */
public class Unit {
    private String prefix;
    private Double value;

    public Unit(String prefix, Double value) {
        this.prefix = prefix;
        this.value = value;
    }

    public String getPrefix() {
        return prefix;
    }

    public Double getValue() {
        return value;
    }
}
