package net.iponweb.disthene.reader.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class XAxisConfigProvider {
    public static final long SEC = 1;
    public static final long MIN = 60;
    public static final long HOUR = MIN * 60;
    public static final long DAY = HOUR * 24;
    public static final long WEEK = DAY * 7;
    public static final long MONTH = DAY * 31;
    public static final long YEAR = DAY * 365;

    private static List<XAxisConfig> configs = new ArrayList<>();

    static {
        configs.add(new XAxisConfig(0, SEC, 5, MIN, 1, SEC, 5, "HH:mm:ss", 10 * MIN));
        configs.add(new XAxisConfig(0.07, SEC, 10, MIN, 1, SEC, 10, "HH:mm:ss", 20 * MIN));
        configs.add(new XAxisConfig(0.14, SEC, 15, MIN, 1, SEC, 15, "HH:mm:ss", 30 * MIN));
        configs.add(new XAxisConfig(0.27, SEC, 30, MIN, 2, MIN, 1, "H:mm", 2 * HOUR));
        configs.add(new XAxisConfig(0.5, MIN, 1, MIN, 2, MIN, 1, "HH:mm", 2 * HOUR));
        configs.add(new XAxisConfig(1.2, MIN, 1, MIN, 4, MIN, 2, "HH:mm", 3 * HOUR));
        configs.add(new XAxisConfig(2, MIN, 1, MIN, 10, MIN, 5, "HH:mm", 6 * HOUR));
        configs.add(new XAxisConfig(5, MIN, 2, MIN, 10, MIN, 10, "HH:mm", 12 * HOUR));
        configs.add(new XAxisConfig(10, MIN, 5, MIN, 20, MIN, 20, "HH:mm", 1 * DAY));
        configs.add(new XAxisConfig(30, MIN, 10, HOUR, 1, HOUR, 1, "HH:mm", 2 * DAY));
        configs.add(new XAxisConfig(60, MIN, 30, HOUR, 2, HOUR, 2, "HH:mm", 2 * DAY));
        configs.add(new XAxisConfig(100, HOUR, 2, HOUR, 4, HOUR, 4, "EEE ha", 6 * DAY));
        configs.add(new XAxisConfig(255, HOUR, 6, HOUR, 12, HOUR, 12, "MM/dd hha", 10 * DAY));
        configs.add(new XAxisConfig(600, HOUR, 6, DAY, 1, DAY, 1, "MM/dd", 14 * DAY));
        configs.add(new XAxisConfig(600, HOUR, 12, DAY, 1, DAY, 1, "MM/dd", 365 * DAY));
        configs.add(new XAxisConfig(2000, DAY, 1, DAY, 2, DAY, 2, "MM/dd", 365 * DAY));
        configs.add(new XAxisConfig(4000, DAY, 2, DAY, 4, DAY, 4, "MM/dd", 365 * DAY));
        configs.add(new XAxisConfig(8000, DAY, 3.5, DAY, 7, DAY, 7, "MM/dd", 365 * DAY));
        configs.add(new XAxisConfig(16000, DAY, 7, DAY, 14, DAY, 14, "MM/dd", 365 * DAY));
        configs.add(new XAxisConfig(32000, DAY, 15, DAY, 30, DAY, 30, "MM/dd", 365 * DAY));
        configs.add(new XAxisConfig(64000, DAY, 30, DAY, 60, DAY, 60, "MM/dd YYYY", Long.MAX_VALUE));
        configs.add(new XAxisConfig(100000, DAY, 60, DAY, 120, DAY, 120, "MM/dd YYYY", Long.MAX_VALUE));
        configs.add(new XAxisConfig(120000, DAY, 120, DAY, 240, DAY, 240, "MM/dd YYYY", Long.MAX_VALUE));
    }

    public static XAxisConfig getXAxisConfig(double secondsPerPixel, long timeRange) {
        XAxisConfig result = null;

        for (XAxisConfig config : configs) {
            if ((config.getSeconds() <= secondsPerPixel) && (config.getMaxInterval() >= timeRange)) {
                result = config;
            }
        }

        return result != null ? result : configs.get(configs.size() - 1);

    }

}