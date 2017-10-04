package org.xbib.io.ftp.client.parser;


import org.xbib.io.ftp.client.FTPFile;
import org.xbib.io.ftp.client.FTPFileEntryParser;

/**
 *
 */
public class OS2FTPEntryParserTest extends FTPParseTestFramework {

    private static final String[] badsamples = {
        "                 DIR   12-30-97   12:32  jbrekke",
        "     0    rsa    DIR   11-25-97   09:42  junk",
        "     0           dir   05-12-97   16:44  LANGUAGE",
        "     0           DIR   13-05-97   25:49  MPTN",
        "587823    RSA    DIR   Jan-08-97   13:58  OS2KRNL",
       // " 33280      A          1997-02-03  13:49  OS2LDR",
        "12-05-96  05:03PM       <DIR>          absoft2",
        "11-14-97  04:21PM                  953 AUDITOR3.INI"
    };
    private static final String[] goodsamples = {
        "     0           DIR   12-30-97   12:32  jbrekke",
        "     0           DIR   11-25-97   09:42  junk",
        "     0           DIR   05-12-97   16:44  LANGUAGE",
        "     0           DIR   05-19-97   12:56  local",
        "     0           DIR   05-12-97   16:52  Maintenance Desktop",
        "     0           DIR   05-13-97   10:49  MPTN",
        "587823    RSA    DIR   01-08-97   13:58  OS2KRNL",
        " 33280      A          02-09-97   13:49  OS2LDR",
        "     0           DIR   11-28-97   09:42  PC",
        "149473      A          11-17-98   16:07  POPUPLOG.OS2",
        "     0           DIR   05-12-97   16:44  PSFONTS",
        "     0           DIR   05-19-2000 12:56  local",
    };

    public OS2FTPEntryParserTest(String name)
    {
        super(name);
    }

    @Override
    public void testParseFieldsOnDirectory() throws Exception
    {
        FTPFile dir = getParser().parseFTPEntry("     0           DIR   11-28-97   09:42  PC");
        assertNotNull("Could not parse entry.", dir);
        assertTrue("Should have been a directory.",
                   dir.isDirectory());
        assertEquals(0,dir.getSize());
        assertEquals("PC", dir.getName());
        assertNotNull("Could not parse time stamp.", dir.getTimestamp());
        assertEquals("Fri Nov 28 09:42:00 1997", dir.getTimestamp().format(df));
    }

    @Override
    public void testParseFieldsOnFile() throws Exception
    {
        FTPFile file = getParser().parseFTPEntry("5000000000      A          11-17-98   16:07  POPUPLOG.OS2");
        assertNotNull("Could not parse entry.", file);
        assertTrue("Should have been a file.",
                   file.isFile());
        assertEquals(5000000000L, file.getSize());
        assertEquals("POPUPLOG.OS2", file.getName());
        assertNotNull("Could not parse time stamp.", file.getTimestamp());
        assertEquals("Tue Nov 17 16:07:00 1998", file.getTimestamp().format(df));
    }

    @Override
    protected String[] getBadListing()
    {

        return (badsamples);
    }

    @Override
    protected String[] getGoodListing()
    {

        return (goodsamples);
    }

    @Override
    protected FTPFileEntryParser getParser() {
        ConfigurableFTPFileEntryParserImpl parser = new OS2FTPEntryParser();
        parser.configure(null);
        return parser;
    }

    @Override
    public void testDefaultPrecision() {
        testPrecision("     0           DIR   05-12-97   16:44  PSFONTS", TimeUnits.MINUTE);
        testPrecision("     0           DIR   05-19-2000 12:56  local", TimeUnits.MINUTE);
    }

    @Override
    public void testRecentPrecision() {
        // Not needed
    }
}
