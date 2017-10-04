package org.xbib.io.ftp.client.parser;

import org.xbib.io.ftp.client.FTPFile;
import org.xbib.io.ftp.client.FTPFileEntryParser;

import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 */
public class OS400FTPEntryParserAdditionalTest extends CompositeFTPParseTestFramework {

    private static final String[][] badsamples = {
            {
        "QPGMR          135168 04/03/18 13:18:19 *FILE",
        "QPGMR          135168    03/24 13:18:19 *FILE",
        "QPGMR          135168 04/03/18 30:06:29 *FILE",
        "QPGMR                 04/03/18 13:18:19 *FILE      RPGUNITC1.FILE",
        "QPGMR          135168    03/24 13:18:19 *FILE      RPGUNITC1.FILE",
        "QPGMR          135168 04/03/18 30:06:29 *FILE      RPGUNITC1.FILE",
        "QPGMR                                   *MEM       ",
        "QPGMR          135168 04/03/18 13:18:19 *MEM       RPGUNITC1.FILE/RUCALLTST.MBR",
        "QPGMR          135168                   *MEM       RPGUNITC1.FILE/RUCALLTST.MBR",
        "QPGMR                 04/03/18 13:18:19 *MEM       RPGUNITC1.FILE/RUCALLTST.MBR",
        "QPGMR USR                               *MEM       RPGUNITC1.FILE/RUCALLTST.MBR"
            }
    };

    private static final String[][] goodsamples = {
            {
        "QPGMR                                   *MEM       RPGUNITC1.FILE/RUCALLTST.MBR",
        "QPGMR        16347136 29.06.13 15:45:09 *FILE      RPGUNIT.SAVF"
            }
    };

    public OS400FTPEntryParserAdditionalTest(String name)
    {
        super(name);
    }

    @Override
    protected String[][] getBadListings()
    {
        return badsamples;
    }

    @Override
    protected String[][] getGoodListings()
    {
        return goodsamples;
    }

    @Override
    protected FTPFileEntryParser getParser() {
        return new CompositeFileEntryParser(new FTPFileEntryParser[] {
            new OS400FTPEntryParser(),
            new UnixFTPEntryParser()
        });
    }

    @Override
    public void testParseFieldsOnDirectory() throws Exception {
        FTPFile f = getParser().parseFTPEntry("PEP             36864 04/03/24 14:06:34 *DIR       dir1/");
        assertNotNull("Could not parse entry.",
                      f);
        assertTrue("Should have been a directory.",
                   f.isDirectory());
        assertEquals("PEP",
                     f.getUser());
        assertEquals("dir1",
                     f.getName());
        assertEquals(36864,
                     f.getSize());
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2004,
                Month.MARCH.getValue(), 24, 14, 6, 34, 0, ZoneId.of("UTC"));
        assertEquals(zonedDateTime, f.getTimestamp());

    }

    @Override
    protected void doAdditionalGoodTests(String test, FTPFile f)
    {
        if (test.startsWith("d"))
        {
            assertEquals("directory.type",
                FTPFile.DIRECTORY_TYPE, f.getType());
        }
    }

    @Override
    public void testParseFieldsOnFile() throws Exception
    {
        FTPFile f = getParser().parseFTPEntry("PEP              5000000000 04/03/24 14:06:29 *STMF      build.xml");
        assertNotNull("Could not parse entry.",
                      f);
        assertTrue("Should have been a file.",
                   f.isFile());
        assertEquals("PEP",
                     f.getUser());
        assertEquals("build.xml",
                     f.getName());
        assertEquals(5000000000L,
                     f.getSize());

        ZonedDateTime zonedDateTime = ZonedDateTime.of(2004,
                Month.MARCH.getValue(), 24, 14, 6, 29, 0, ZoneId.of("UTC"));
        assertEquals(zonedDateTime, f.getTimestamp());

    }

    @Override
    public void testDefaultPrecision() {
        // Done in other class
    }

    @Override
    public void testRecentPrecision() {
        // Done in other class
    }
}
