package org.xbib.io.ftp.client.parser;

import org.xbib.io.ftp.client.Configurable;
import org.xbib.io.ftp.client.FTPClientConfig;

import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 *
 * Note: 'uuuu' in patterns represents ChronoField.YEAR as opposed to 'yyyy' that represents ChronoField.YEAR_OF_ERA.
 * This is important when withResolverStyle(ResolverStyle.STRICT) is used.
 * */
public class ZonedDateTimeParser implements FTPTimestampParser, Configurable {

    private static final Logger logger = Logger.getLogger(ZonedDateTimeParser.class.getName());

    private static final List<DateTimeFormatter> FORMATTER_LIST = Arrays.asList(
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM dd HH:mm")
                    .parseDefaulting(ChronoField.YEAR, Year.now().getValue())
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withLocale(Locale.US)
                    .withZone(ZoneId.of("UTC")),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM d HH:mm")
                    .parseDefaulting(ChronoField.YEAR, Year.now().getValue())
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withLocale(Locale.US)
                    .withZone(ZoneId.of("UTC")),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM dd H:mm")
                    .parseDefaulting(ChronoField.YEAR, Year.now().getValue())
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withLocale(Locale.US)
                    .withZone(ZoneId.of("UTC")),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM d H:mm")
                    .parseDefaulting(ChronoField.YEAR, Year.now().getValue())
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withLocale(Locale.US)
                    .withZone(ZoneId.of("UTC")),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM  d HH:mm")
                    .parseDefaulting(ChronoField.YEAR, Year.now().getValue())
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withLocale(Locale.US)
                    .withZone(ZoneId.of("UTC")),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM dd yyyy HH:mm")
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withLocale(Locale.US)
                    .withZone(ZoneId.of("UTC")),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM  d yyyy HH:mm")
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withLocale(Locale.US)
                    .withZone(ZoneId.of("UTC")),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM dd yyyy")
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM  d yyyy")
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM dd  yyyy")
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM  d  yyyy")
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("MM-dd-yy HH:mm")
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("MM-dd-yyyy hh:mma")
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("MM-dd-yy hh:mma")
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("MM-dd-yyyy kk:mm:ss")
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("MM-dd-yy kk:mm:ss")
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("MM-dd-yyyy kk:mm")
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("MM-dd-yy kk:mm")
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd-MMM-yyyy HH:mm:ss")
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("d-MMM-yyyy HH:mm:ss")
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd MMM HH:mm")
                    .parseDefaulting(ChronoField.YEAR, Year.now().getValue())
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("dd-MM-yy hh:mma")
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("dd-MM-yy")
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("M'\u6708' d'\u65e5' HH:mm")
                    .parseDefaulting(ChronoField.YEAR, Year.now().getValue())
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("M'\u6708' d'\u65e5' yyyy'\u5e74'")
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm")
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US),
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy/MM/dd HH:mm")
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US)
    );

    private DateTimeFormatter customDateTimeFormatter;

    @Override
    public void configure(FTPClientConfig config) {
        String pattern = config.getDefaultDateFormatStr();
        DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder();
        if (pattern != null) {
            dateTimeFormatterBuilder.appendPattern(pattern);
            if (!pattern.contains("yy")) {
                dateTimeFormatterBuilder.parseDefaulting(ChronoField.YEAR, Year.now().getValue());
            }
        }
        dateTimeFormatterBuilder.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0);
        customDateTimeFormatter = dateTimeFormatterBuilder
                .toFormatter()
                .withZone(ZoneId.of("UTC"))
                .withLocale(Locale.US);
    }

    @Override
    public ZonedDateTime parseTimestamp(String timestampStr) {
        DateTimeParseException exception = null;
        for (DateTimeFormatter df : FORMATTER_LIST) {
            try {
                ZonedDateTime zonedDateTime =  ZonedDateTime.parse(timestampStr, df);
                if (zonedDateTime.getYear() > Year.now().getValue()) {
                    zonedDateTime = zonedDateTime.minusYears(100);
                }
                return zonedDateTime;
            } catch (DateTimeParseException e1) {
                if (exception == null) {
                    exception = e1;
                }
            }
        }
        logger.warning("unknown time stamp: " + timestampStr);
        if (customDateTimeFormatter != null) {
            try {
                return ZonedDateTime.parse(timestampStr, customDateTimeFormatter);
            } catch (DateTimeParseException e1) {
                exception = e1;
            }
        }
        if (exception != null) {
            throw exception;
        }
        return null;
    }
}
