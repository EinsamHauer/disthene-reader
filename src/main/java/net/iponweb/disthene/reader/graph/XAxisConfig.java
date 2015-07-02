package net.iponweb.disthene.reader.graph;

/**
 * @author Andrei Ivanov
 */
public class XAxisConfig {
    private double seconds;
    private long minorGridUnit;
    private double minorGridStep;
    private long majorGridUnit;
    private int majorGridStep;
    private long labelUnit;
    private int labelStep;
    private String format; //??
    private long maxInterval;

    public XAxisConfig(double seconds, long minorGridUnit, double minorGridStep, long majorGridUnit, int majorGridStep, long labelUnit, int labelStep, String format, long maxInterval) {
        this.seconds = seconds;
        this.minorGridUnit = minorGridUnit;
        this.minorGridStep = minorGridStep;
        this.majorGridUnit = majorGridUnit;
        this.majorGridStep = majorGridStep;
        this.labelUnit = labelUnit;
        this.labelStep = labelStep;
        this.format = format;
        this.maxInterval = maxInterval;
    }

    @Override
    public String toString() {
        return "XAxisConfig{" +
                "seconds=" + seconds +
                ", minorGridUnit=" + minorGridUnit +
                ", minorGridStep=" + minorGridStep +
                ", majorGridUnit=" + majorGridUnit +
                ", majorGridStep=" + majorGridStep +
                ", labelUnit=" + labelUnit +
                ", labelStep=" + labelStep +
                ", format='" + format + '\'' +
                ", maxInterval=" + maxInterval +
                '}';
    }

    public double getSeconds() {
        return seconds;
    }

    public void setSeconds(double seconds) {
        this.seconds = seconds;
    }

    public long getMinorGridUnit() {
        return minorGridUnit;
    }

    public void setMinorGridUnit(long minorGridUnit) {
        this.minorGridUnit = minorGridUnit;
    }

    public double getMinorGridStep() {
        return minorGridStep;
    }

    public void setMinorGridStep(double minorGridStep) {
        this.minorGridStep = minorGridStep;
    }

    public long getMajorGridUnit() {
        return majorGridUnit;
    }

    public void setMajorGridUnit(long majorGridUnit) {
        this.majorGridUnit = majorGridUnit;
    }

    public int getMajorGridStep() {
        return majorGridStep;
    }

    public void setMajorGridStep(int majorGridStep) {
        this.majorGridStep = majorGridStep;
    }

    public long getLabelUnit() {
        return labelUnit;
    }

    public void setLabelUnit(long labelUnit) {
        this.labelUnit = labelUnit;
    }

    public int getLabelStep() {
        return labelStep;
    }

    public void setLabelStep(int labelStep) {
        this.labelStep = labelStep;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public long getMaxInterval() {
        return maxInterval;
    }

    public void setMaxInterval(long maxInterval) {
        this.maxInterval = maxInterval;
    }
}
