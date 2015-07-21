package net.iponweb.disthene.reader.utils;

import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.utils.Span;
import net.iponweb.disthene.reader.exceptions.InvalidParameterValueException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author Andrei Ivanov
 */
public class DateUtils {

    public static long parseDateTime(String in, DateTimeZone tz) throws InvalidParameterValueException {
        Span span = Chronic.parse(in);
        if (span == null) {
            throw new InvalidParameterValueException("Unsupported date format: " + in);
        }

        return new DateTime(span.getBeginCalendar().getTimeInMillis(), tz).getMillis() / 1000L;
    }
}
