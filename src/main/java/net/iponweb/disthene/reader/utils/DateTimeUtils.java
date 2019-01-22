package net.iponweb.disthene.reader.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andrei Ivanov
 */
public class DateTimeUtils {

    private static Pattern timeOffsetPattern = Pattern.compile("^([+-]?)(\\d+)([a-z]+)$");

    /**
     * Parses time offset from string (Examples: "-1d", "+1mon")
     *
     * @param s string to parse
     * @return number of seconds in the offset
     */
    public static Long parseTimeOffset(String s) {
        Matcher matcher = timeOffsetPattern.matcher(s.replaceAll("^['\"]|['\"]$", ""));

        if (!matcher.matches()) return 0L;

        int sign = matcher.group(1).equals("+") ? 1 : -1;
        long offset = Integer.parseInt(matcher.group(2)) * getUnitValue(matcher.group(3));

        return offset * sign;
    }

    private static long getUnitValue(String s) {
        if (s.startsWith("s")) {
            return 1;
        } else if (s.startsWith("min")) {
            return 60;
        } else if (s.startsWith("h")) {
            return 3600;
        } else if (s.startsWith("d")) {
            return 86400;
        } else if (s.startsWith("w")) {
            return 604800;
        } else if (s.startsWith("mon")) {
            return 18144000;
        } else if (s.startsWith("y")) {
            return 31536000;
        } else {
            return 60;
        }
    }

    public static boolean testTimeOffset(String s) {
        return timeOffsetPattern.matcher(s.replaceAll("^['\"]|['\"]$", "")).matches();
    }
}
