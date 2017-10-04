package org.xbib.io.ftp.client.parser;

import org.xbib.io.ftp.client.FTPFile;
import org.xbib.io.ftp.client.FTPFileEntryParser;

import java.time.Month;
import java.time.ZonedDateTime;


/**
 * Tests the EnterpriseUnixFTPEntryParser
 */
public class EnterpriseUnixFTPEntryParserTest extends FTPParseTestFramework
{

    private static final String[] BADSAMPLES =
    {
        "zrwxr-xr-x   2 root     root         4096 Mar  2 15:13 zxbox",
        "dxrwr-xr-x   2 root     root         4096 Aug 24  2001 zxjdbc",
        "drwxr-xr-x   2 root     root         4096 Jam  4 00:03 zziplib",
        "drwxr-xr-x   2 root     99           4096 Feb 23 30:01 zzplayer",
        "drwxr-xr-x   2 root     root         4096 Aug 36  2001 zztpp",
        "-rw-r--r--   1 14       staff       80284 Aug 22  zxJDBC-1.2.3.tar.gz",
        "-rw-r--r--   1 14       staff      119:26 Aug 22  2000 zxJDBC-1.2.3.zip",
        "-rw-r--r--   1 ftp      no group    83853 Jan 22  2001 zxJDBC-1.2.4.tar.gz",
        "-rw-r--r--   1ftp       nogroup    126552 Jan 22  2001 zxJDBC-1.2.4.zip",
        "-rw-r--r--   1 root     root       111325 Apr -7 18:79 zxJDBC-2.0.1b1.tar.gz",
        "drwxr-xr-x   2 root     root         4096 Mar  2 15:13 zxbox",
        "drwxr-xr-x 1 usernameftp 512 Jan 29 23:32 prog",
        "drwxr-xr-x   2 root     root         4096 Aug 24  2001 zxjdbc",
        "drwxr-xr-x   2 root     root         4096 Jan  4 00:03 zziplib",
        "drwxr-xr-x   2 root     99           4096 Feb 23  2001 zzplayer",
        "drwxr-xr-x   2 root     root         4096 Aug  6  2001 zztpp",
        "-rw-r--r--   1 14       staff       80284 Aug 22  2000 zxJDBC-1.2.3.tar.gz",
        "-rw-r--r--   1 14       staff      119926 Aug 22  2000 zxJDBC-1.2.3.zip",
        "-rw-r--r--   1 ftp      nogroup     83853 Jan 22  2001 zxJDBC-1.2.4.tar.gz",
        "-rw-r--r--   1 ftp      nogroup    126552 Jan 22  2001 zxJDBC-1.2.4.zip",
        "-rw-r--r--   1 root     root       111325 Apr 27  2001 zxJDBC-2.0.1b1.tar.gz",
        "-rw-r--r--   1 root     root       190144 Apr 27  2001 zxJDBC-2.0.1b1.zip",
        "drwxr-xr-x   2 root     root         4096 Aug 26  20 zztpp",
        "drwxr-xr-x   2 root     root         4096 Aug 26  201 zztpp",
        "drwxr-xr-x   2 root     root         4096 Aug 26  201O zztpp", // OH not zero
    };
    private static final String[] GOODSAMPLES =
    {
        "-C--E-----FTP B QUA1I1      18128       41 Aug 12 13:56 QUADTEST",
        "-C--E-----FTP A QUA1I1      18128       41 Aug 12 13:56 QUADTEST2",
        "-C--E-----FTP A QUA1I1      18128       41 Apr 1 2014 QUADTEST3"
    };

    /**
     * Creates a new EnterpriseUnixFTPEntryParserTest object.
     *
     * @param name Test name.
     */
    public EnterpriseUnixFTPEntryParserTest(String name)
    {
        super(name);
    }

    @Override
    public void testParseFieldsOnDirectory() throws Exception
    {
        // Everything is a File for now.
    }

    @Override
    public void testParseFieldsOnFile() throws Exception
    {
        FTPFile file = getParser().parseFTPEntry("-C--E-----FTP B QUA1I1      18128       5000000000 Aug 12 13:56 QUADTEST");
        int year        = ZonedDateTime.now().getYear();

        assertTrue("Should be a file.", file.isFile());
        assertEquals("QUADTEST", file.getName());
        assertEquals(5000000000L, file.getSize());
        assertEquals("QUA1I1", file.getUser());
        assertEquals("18128", file.getGroup());

        if (ZonedDateTime.now().getMonth().getValue() <  Month.AUGUST.getValue()) {
            --year;
        }

        ZonedDateTime timestamp = file.getTimestamp();
        assertEquals(year, timestamp.getYear());
        assertEquals(Month.AUGUST, timestamp.getMonth());
        assertEquals(12, timestamp.getDayOfMonth());
        assertEquals(13, timestamp.getHour());
        assertEquals(56, timestamp.getMinute());
        assertEquals(0, timestamp.getSecond());

        checkPermisions(file);
    }

    @Override
    public void testRecentPrecision() {
        testPrecision("-C--E-----FTP B QUA1I1      18128       5000000000 Aug 12 13:56 QUADTEST", TimeUnits.MINUTE);
    }

    @Override
    public void testDefaultPrecision() {
        testPrecision("-C--E-----FTP B QUA1I1      18128       5000000000 Aug 12 2014 QUADTEST", TimeUnits.DAY);
    }

    @Override
    protected String[] getBadListing()
    {
        return (BADSAMPLES);
    }

    @Override
    protected String[] getGoodListing()
    {
        return (GOODSAMPLES);
    }

    @Override
    protected FTPFileEntryParser getParser()
    {
        return (new EnterpriseUnixFTPEntryParser());
    }

    /**
     * Method checkPermisions. Verify that the parser does NOT  set the
     * permissions.
     *
     * @param dir
     */
    private void checkPermisions(FTPFile dir)
    {
        assertTrue("Owner should not have read permission.",
                   !dir.hasPermission(FTPFile.USER_ACCESS,
                                      FTPFile.READ_PERMISSION));
        assertTrue("Owner should not have write permission.",
                   !dir.hasPermission(FTPFile.USER_ACCESS,
                                      FTPFile.WRITE_PERMISSION));
        assertTrue("Owner should not have execute permission.",
                   !dir.hasPermission(FTPFile.USER_ACCESS,
                                      FTPFile.EXECUTE_PERMISSION));
        assertTrue("Group should not have read permission.",
                   !dir.hasPermission(FTPFile.GROUP_ACCESS,
                                      FTPFile.READ_PERMISSION));
        assertTrue("Group should not have write permission.",
                   !dir.hasPermission(FTPFile.GROUP_ACCESS,
                                      FTPFile.WRITE_PERMISSION));
        assertTrue("Group should not have execute permission.",
                   !dir.hasPermission(FTPFile.GROUP_ACCESS,
                                      FTPFile.EXECUTE_PERMISSION));
        assertTrue("World should not have read permission.",
                   !dir.hasPermission(FTPFile.WORLD_ACCESS,
                                      FTPFile.READ_PERMISSION));
        assertTrue("World should not have write permission.",
                   !dir.hasPermission(FTPFile.WORLD_ACCESS,
                                      FTPFile.WRITE_PERMISSION));
        assertTrue("World should not have execute permission.",
                   !dir.hasPermission(FTPFile.WORLD_ACCESS,
                                      FTPFile.EXECUTE_PERMISSION));
    }
}
