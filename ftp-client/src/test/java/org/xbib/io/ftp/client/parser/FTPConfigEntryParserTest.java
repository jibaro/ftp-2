package org.xbib.io.ftp.client.parser;

import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import junit.framework.TestCase;
import org.xbib.io.ftp.client.FTPClientConfig;
import org.xbib.io.ftp.client.FTPFile;

/**
 * This is a simple TestCase that tests entry parsing using the new FTPClientConfig
 * mechanism. The normal FTPClient cannot handle the different date formats in these
 * entries, however using a configurable format, we can handle it easily.
 *
 * The original system presenting this issue was an AIX system - see bug #27437 for details.
 */
public class FTPConfigEntryParserTest extends TestCase {

    public void testParseFieldsOnAIX() {

        // Set a date format for this server type
        FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        config.setDefaultDateFormatStr("dd MMM HH:mm");

        UnixFTPEntryParser parser = new UnixFTPEntryParser();
        parser.configure(config);

        FTPFile f = parser.parseFTPEntry("-rw-r-----   1 ravensm  sca          814 02 Mar 16:27 ZMIR2.m");

        assertNotNull("Could not parse entry.", f);
        assertFalse("Is not a directory.", f.isDirectory());

        assertTrue("Should have user read permission.", f.hasPermission(
                FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should have user write permission.", f.hasPermission(
                FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse("Should NOT have user execute permission.", f
                .hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have group read permission.", f.hasPermission(
                FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertFalse("Should NOT have group write permission.", f
                .hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse("Should NOT have group execute permission.",
                f.hasPermission(FTPFile.GROUP_ACCESS,
                        FTPFile.EXECUTE_PERMISSION));
        assertFalse("Should NOT have world read permission.", f.hasPermission(
                FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertFalse("Should NOT have world write permission.", f
                .hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse("Should NOT have world execute permission.",
                f.hasPermission(FTPFile.WORLD_ACCESS,
                        FTPFile.EXECUTE_PERMISSION));

        assertEquals(1, f.getHardLinkCount());

        assertEquals("ravensm", f.getUser());
        assertEquals("sca", f.getGroup());

        assertEquals("ZMIR2.m", f.getName());
        assertEquals(814, f.getSize());

        ZonedDateTime zonedDateTime = ZonedDateTime.of(Year.now().getValue(), 3, 2, 16, 27, 0, 0, ZoneId.of("UTC"));

        assertEquals(zonedDateTime, f.getTimestamp());
    }

    /**
     * This is a new format reported on the mailing lists. Parsing this kind of
     * entry necessitated changing the regex in the parser.
     *
     */
    public void testParseEntryWithSymlink() {

        FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        config.setDefaultDateFormatStr("yyyy-MM-dd HH:mm");

        UnixFTPEntryParser parser = new UnixFTPEntryParser();
        parser.configure(config);

        FTPFile f = parser.parseFTPEntry("lrwxrwxrwx   1 neeme neeme    23 2005-03-02 18:06 macros");

        assertNotNull("Could not parse entry.", f);
        assertFalse("Is not a directory.", f.isDirectory());
        assertTrue("Is a symbolic link", f.isSymbolicLink());

        assertTrue("Should have user read permission.", f.hasPermission(
                FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should have user write permission.", f.hasPermission(
                FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have user execute permission.", f
                .hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have group read permission.", f.hasPermission(
                FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should have group write permission.", f
                .hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have group execute permission.",
                f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have world read permission.", f.hasPermission(
                FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should have world write permission.", f
                .hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have world execute permission.",
                f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));

        assertEquals(1, f.getHardLinkCount());

        assertEquals("neeme", f.getUser());
        assertEquals("neeme", f.getGroup());

        assertEquals("macros", f.getName());
        assertEquals(23, f.getSize());

        ZonedDateTime zonedDateTime = ZonedDateTime.of(2005, 3, 2, 18, 6, 0, 0, ZoneId.of("UTC"));
        assertEquals(zonedDateTime, f.getTimestamp());

    }

}
