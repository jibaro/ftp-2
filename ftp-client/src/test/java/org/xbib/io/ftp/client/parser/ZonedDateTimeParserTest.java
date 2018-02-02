package org.xbib.io.ftp.client.parser;

import org.junit.Test;

import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class ZonedDateTimeParserTest {

    @Test
    public void test1() {
        Year year = Year.now();
        DateTimeFormatter df = new DateTimeFormatterBuilder()
                .appendPattern("MMM dd HH:mm")
                .parseDefaulting(ChronoField.YEAR, year.getValue())
                .toFormatter()
                .withLocale(Locale.US)
                .withZone(ZoneId.of("UTC"));
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("Oct 01 21:15", df);
        assertEquals(year.getValue() + "-10-01T21:15Z[UTC]", zonedDateTime.toString());
    }

    @Test
    public void test2() {
        DateTimeFormatter df = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("d-MMM-yyyy HH:mm:ss")
                .toFormatter()
                .withLocale(Locale.US)
                .withZone(ZoneId.of("UTC"));
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2-JUN-1998 07:32:04", df);
        assertEquals("1998-06-02T07:32:04Z[UTC]", zonedDateTime.toString());
    }
}
