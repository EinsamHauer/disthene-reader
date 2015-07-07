package net.iponweb.disthene.reader.utils;

import sun.misc.Regexp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andrei Ivanov
 */
public class DateTimeUtils {

    private static Pattern timeOffsetPattern = Pattern.compile("^([+-]?)(\\d+)(s|min|h|d|w|mon|y)$");

    /**
     * Parses time offset from string (Examples: "-1d", "+1mon")
     *
     * @param s string to parse
     * @return number of seconds in the offset
     */
    public static Long parseTimeOffset(String s) {
        Matcher matcher = timeOffsetPattern.matcher(s);

        if (!matcher.matches()) return 0L;

        int sign = matcher.group(1).equals("+") ? 1 : -1;
        long offset = Integer.parseInt(matcher.group(2));

        switch (matcher.group(3)) {
            case "min":
                offset *= 60;
                break;
            case "h":
                offset *= 3600;
                break;
            case "d":
                offset *= 86400;
                break;
            case "w":
                offset *= 604800;
                break;
            case "mon":
                offset *= 18144000;
                break;
            case "y":
                offset *= 31536000;
                break;
        }

        return offset * sign;
    }

    public static boolean testTimeOffset(String s) {
        return timeOffsetPattern.matcher(s).matches();
    }
}
