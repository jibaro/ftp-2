package org.xbib.io.ftp.client;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A functional test suite for checking that site listings work.
 */
public class ListingFunctionalTest extends TestCase
{
    // Offsets within testData below
    static final int HOSTNAME = 0;
    static final int VALID_PARSERKEY = 1;
    static final int INVALID_PARSERKEY = 2;
    //static final int INVALID_PATH = 3;
    static final int VALID_FILENAME = 4;
    static final int VALID_PATH = 5;
    static final int PATH_PWD = 6; // response to PWD

    public static final Test suite()
    {
        String[][] testData =
            {
                {
                    "ftp.ibiblio.org", "unix", "vms",
                    "HA!", "javaio.jar",
                    "pub/languages/java/javafaq",
                    "/pub/languages/java/javafaq",
                },
                {
                    "apache.cs.utah.edu", "unix", "vms",
                    "HA!", "HEADER.html",
                    "apache.org",
                    "/apache.org",
                },
//                { // not available
//                    "ftp.wacom.com", "windows", "VMS", "HA!",
//                    "wacom97.zip", "pub\\drivers"
//                },
                /*{
                    "ftp.decuslib.com", "vms", "windows", // VMS OpenVMS V8.3
                    "[.HA!]", "FREEWARE_SUBMISSION_INSTRUCTIONS.TXT;1",
                    "[.FREEWAREV80.FREEWARE]",
                    "DECUSLIB:[DECUS.FREEWAREV80.FREEWARE]"
                },*/
//                {  // VMS TCPware V5.7-2 does not return (RWED) permissions
//                    "ftp.process.com", "vms", "windows",
//                    "[.HA!]", "MESSAGE.;1",
//                    "[.VMS-FREEWARE.FREE-VMS]" //
//                },
            };
        Class<?> clasz = ListingFunctionalTest.class;
        Method[] methods = clasz.getDeclaredMethods();
        TestSuite allSuites = new TestSuite("FTP Listing Functional Test Suite");

        for (String[] element : testData)
        {
            TestSuite suite = new TestSuite(element[VALID_PARSERKEY]+ " @ " +element[HOSTNAME]);

            for (Method method : methods)
            {
                if (method.getName().startsWith("test"))
                {
                    suite.addTest(new ListingFunctionalTest(method.getName(), element));
                }
            }

            allSuites.addTest(suite);
        }

        return allSuites;
    }

    private FTPClient client;
    private final String hostName;
    private final String invalidParserKey;
    //private final String invalidPath;
    private final String validFilename;
    private final String validParserKey;
    private final String validPath;
    private final String pwdPath;

    public ListingFunctionalTest(String arg0, String[] settings)
    {
        super(arg0);
        invalidParserKey = settings[INVALID_PARSERKEY];
        validParserKey = settings[VALID_PARSERKEY];
        //invalidPath = settings[INVALID_PATH];
        validFilename = settings[VALID_FILENAME];
        validPath = settings[VALID_PATH];
        pwdPath = settings[PATH_PWD];
        hostName = settings[HOSTNAME];
    }

    private boolean findByName(List<?> fileList, String string)
    {
        boolean found = false;
        Iterator<?> iter = fileList.iterator();

        while (iter.hasNext() && !found)
        {
            Object element = iter.next();

            if (element instanceof FTPFile)
            {
                FTPFile file = (FTPFile) element;

                found = file.getName().equals(string);
            }
            else
            {
                String filename = (String) element;

                found = filename.endsWith(string);
            }
        }

        return found;
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        client = new FTPClient();
        client.connect(hostName);
        client.login("anonymous", "anonymous");
        client.enterLocalPassiveMode();
//        client.addProtocolCommandListener(new PrintCommandListener(System.out));
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown()
        throws Exception
    {
        try
        {
            client.logout();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (client.isConnected())
        {
            client.disconnect();
        }

        client = null;
        super.tearDown();
    }

    /*
     * Test for FTPListParseEngine initiateListParsing()
     */
    public void testInitiateListParsing()
        throws IOException
    {
        client.changeWorkingDirectory(validPath);

        FTPListParseEngine engine = client.initiateListParsing();
        List<FTPFile> files = Arrays.asList(engine.getNext(25));

        assertTrue(files.toString(), findByName(files, validFilename));
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String, String)
     */
    public void testInitiateListParsingWithPath()
        throws IOException
    {
        FTPListParseEngine engine = client.initiateListParsing(validParserKey,
                                                               validPath);
        List<FTPFile> files = Arrays.asList(engine.getNext(25));

        assertTrue(files.toString(), findByName(files, validFilename));
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String)
     */
    public void testInitiateListParsingWithPathAndAutodetection()
        throws IOException
    {
        FTPListParseEngine engine = client.initiateListParsing(validPath);
        List<FTPFile> files = Arrays.asList(engine.getNext(25));

        assertTrue(files.toString(), findByName(files, validFilename));
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String)
     */
    /*public void testInitiateListParsingWithPathAndAutodetectionButEmpty()
        throws IOException
    {
        FTPListParseEngine engine = client.initiateListParsing(invalidPath);

        assertFalse(engine.hasNext());
    }*/

    /*
     * Test for FTPListParseEngine initiateListParsing(String, String)
     */
    /*public void testInitiateListParsingWithPathAndIncorrectParser()
        throws IOException
    {
        FTPListParseEngine engine = client.initiateListParsing(invalidParserKey, invalidPath);

        assertFalse(engine.hasNext());
    }*/

    /*
     * Test for FTPFile[] listFiles(String, String)
     */
    public void testListFiles()
        throws IOException
    {
        FTPClientConfig config = new FTPClientConfig(validParserKey);
        client.configure(config);
        List<FTPFile> files = Arrays.asList(client.listFiles(validPath));

        assertTrue(files.toString(),
                   findByName(files, validFilename));
    }

    public void testListFilesWithAutodection()
        throws IOException
    {
        client.changeWorkingDirectory(validPath);

        List<FTPFile> files = Arrays.asList(client.listFiles());

        assertTrue(files.toString(),
                   findByName(files, validFilename));
    }

    /*
     * Test for FTPFile[] listFiles(String, String)
     */
    public void testListFilesWithIncorrectParser()
        throws IOException
    {
        FTPClientConfig config = new FTPClientConfig(invalidParserKey);
        client.configure(config);

        FTPFile[] files = client.listFiles(validPath);

        assertNotNull(files);

        // This may well fail, e.g. window parser for VMS listing
        assertTrue("Expected empty array: "+Arrays.toString(files), Arrays.equals(new FTPFile[]{}, files));
    }

    /*
     * Test for FTPFile[] listFiles(String)
     */
    /*public void testListFilesWithPathAndAutodectionButEmpty()
        throws IOException
    {
        FTPFile[] files = client.listFiles(invalidPath);

        assertEquals(0, files.length);
    }*/

    /*
     * Test for FTPFile[] listFiles(String)
     */
    public void testListFilesWithPathAndAutodetection()
        throws IOException
    {
        List<FTPFile> files = Arrays.asList(client.listFiles(validPath));

        assertTrue(files.toString(),
                   findByName(files, validFilename));
    }

    /*
     * Test for String[] listNames()
     */
    public void testListNames()
        throws IOException
    {
        client.changeWorkingDirectory(validPath);

        String[] names = client.listNames();

        assertNotNull(names);

        List<String> lnames = Arrays.asList(names);

        assertTrue(lnames.toString(), lnames.contains(validFilename));
    }

    /*
     * Test for String[] listNames(String)
     */
    public void testListNamesWithPath()
        throws IOException
    {
        String[] listNames = client.listNames(validPath);
        assertNotNull("listNames not null", listNames);
        List<String> names = Arrays.asList(listNames);

        assertTrue(names.toString(), findByName(names, validFilename));
    }

    /*public void testListNamesWithPathButEmpty()
        throws IOException
    {
        String[] names = client.listNames(invalidPath);

        assertNull(names);
    }*/

    public void testPrintWorkingDirectory()
            throws IOException
    {
        client.changeWorkingDirectory(validPath);
        String pwd = client.printWorkingDirectory();
        assertEquals(pwdPath, pwd);
    }
}
