package net.iponweb.disthene.reader.beans;

import java.util.Arrays;

/**
 * @author Andrei Ivanov
 */
public class TimeSeries {

    private String name;
    private Long from;
    private Long to;
    private int step;

    private Double[] values = new Double[0];

    public TimeSeries(String name, Long from, Long to, int step) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.step = step;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Double[] getValues() {
        return values;
    }

    public void setValues(Double[] values) {
        this.values = values;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return "TimeSeries{" +
                "name='" + name + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", step=" + step +
                ", values=" + Arrays.toString(values) +
                '}';
    }
}
