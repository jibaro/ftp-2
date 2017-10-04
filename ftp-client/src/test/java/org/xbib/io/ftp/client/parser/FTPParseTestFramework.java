package org.xbib.io.ftp.client.parser;

import junit.framework.TestCase;
import org.xbib.io.ftp.client.FTPFile;
import org.xbib.io.ftp.client.FTPFileEntryParser;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 *
 */
public abstract class FTPParseTestFramework extends TestCase
{
    private FTPFileEntryParser parser = null;

    static final DateTimeFormatter df = DateTimeFormatter
            .ofPattern("EEE MMM dd HH:mm:ss yyyy")
            .withZone(ZoneId.of("UTC"))
            .withLocale(Locale.US);

    public FTPParseTestFramework(String name)
    {
        super(name);
    }

    public void testBadListing() {
        String[] badsamples = getBadListing();
        for (String test : badsamples) {
            try {
                FTPFile f = parser.parseFTPEntry(test);
                assertNull("Should have Failed to parse <" + test + ">", nullFileOrNullDate(f));
                doAdditionalBadTests(test, f);
            } catch (DateTimeParseException e) {
                //
            }
        }
    }

    public void testGoodListing() {
        String[] goodsamples = getGoodListing();
        for (String test : goodsamples) {
            FTPFile f = parser.parseFTPEntry(test);
            assertNotNull("Failed to parse " + test, f);
            doAdditionalGoodTests(test, f);
        }
    }

    /**
     * during processing you could hook here to do additional tests
     *
     * @param test raw entry
     * @param f    parsed entry
     */
    protected void doAdditionalGoodTests(String test, FTPFile f) {
    }

    /**
     * during processing you could hook here to do additional tests
     *
     * @param test raw entry
     * @param f    parsed entry
     */
    protected void doAdditionalBadTests(String test, FTPFile f) {
    }

    /**
     * Method getBadListing.
     * Implementors must provide a listing that contains failures.
     * @return String[]
     */
    protected abstract String[] getBadListing();

    /**
     * Method getGoodListing.
     * Implementors must provide a listing that passes.
     * @return String[]
     */
    protected abstract String[] getGoodListing();

    /**
     * Method getParser.
     * Provide the parser to use for testing.
     * @return FTPFileEntryParser
     */
    protected abstract FTPFileEntryParser getParser();

    /**
     * Method testParseFieldsOnDirectory.
     * Provide a test to show that fields on a directory entry are parsed correctly.
     * @throws Exception on error
     */
    public abstract void testParseFieldsOnDirectory() throws Exception;

    /**
     * Method testParseFieldsOnFile.
     * Provide a test to show that fields on a file entry are parsed correctly.
     * @throws Exception on error
     */
    public abstract void testParseFieldsOnFile() throws Exception;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = getParser();
    }

    /**
     * Check if FTPFile entry parsing failed; i.e. if entry is null or date is null.
     *
     * @param f FTPFile entry - may be null
     * @return null if f is null or the date is null
     */
    protected FTPFile nullFileOrNullDate(FTPFile f) {
        if (f==null){
            return null;
        }
        if (f.getTimestamp() == null) {
            return null;
        }
        return f;
    }

    enum TimeUnits {
        MILLISECOND(ChronoUnit.MILLIS),
        SECOND(ChronoUnit.SECONDS),
        MINUTE(ChronoUnit.MINUTES),
        HOUR(ChronoUnit.HOURS),
        DAY(ChronoUnit.DAYS),
        MONTH(ChronoUnit.MONTHS),
        YEAR(ChronoUnit.YEARS);
        ChronoUnit chronoUnit;
        TimeUnits(ChronoUnit chronoUnit) {
            this.chronoUnit = chronoUnit;
        }
    }

    protected void testPrecision(String listEntry, TimeUnits expectedPrecision) {
        FTPFile file = getParser().parseFTPEntry(listEntry);
        assertNotNull("Could not parse " + listEntry, file);
        ZonedDateTime zonedDateTime = file.getTimestamp();
        assertNotNull("Failed to parse time in " + listEntry, zonedDateTime);
        int ordinal = expectedPrecision.ordinal();
        TimeUnits[] timeUnits = TimeUnits.values();
        for(int i = ordinal; i < timeUnits.length; i++) {
            TimeUnits unit = timeUnits[i];
            assertTrue("Expected set "+unit+" in " + listEntry,
                    zonedDateTime.isSupported(unit.chronoUnit));
        }
    }

    public abstract void testDefaultPrecision();

    public abstract void testRecentPrecision();
}
