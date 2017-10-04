package org.xbib.io.ftp.client;

import java.io.IOException;
import java.net.SocketException;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

/*
 * This test was contributed in a different form by W. McDonald Buck
 * of Boulder, Colorado, to help fix some bugs with the FTPClientConfig
 * in a real world setting.  It is a perfect functional test for the
 * Time Zone functionality of FTPClientConfig.
 *
 * A publicly accessible FTP server at the US National Oceanographic and
 * Atmospheric Adminstration houses a directory which contains
 * 300 files, named sn.0000 to sn.0300. Every ten minutes or so
 * the next file in sequence is rewritten with new data. Thus the directory
 * contains observations for more than 24 hours of data.  Since the server
 * has its clock set to GMT this is an excellent functional test for any
 * machine in a different time zone.
 *
 * Noteworthy is the fact that the ftp routines in some web browsers don't
 * work as well as this.  They can't, since they have no way of knowing the
 * server's time zone.  Depending on the local machine's position relative
 * to GMT and the time of day, the browsers may decide that a timestamp
 * would be in the  future if given the current year, so they assume the
 * year to be  last year.  This illustrates the value of FTPClientConfig's
 * time zone functionality.
 */

public class FTPClientConfigFunctionalTest extends TestCase {

    private final FTPClient ftpClient = new FTPClient();
    private FTPClientConfig ftpClientConfig;

    /**
     *
     */
    public FTPClientConfigFunctionalTest() {
        super();

    }

    /*
     * @throws java.lang.Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ftpClientConfig = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        ftpClientConfig.setServerTimeZoneId("GMT");
        ftpClient.configure(ftpClientConfig);
        try {
            ftpClient.connect("tgftp.nws.noaa.gov");
            ftpClient.login("anonymous","testing@apache.org");
            ftpClient.changeWorkingDirectory("SL.us008001/DF.an/DC.sflnd/DS.metar");
            ftpClient.enterLocalPassiveMode();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
     * @throws java.lang.Exception
     */
    @Override
    protected void tearDown() throws Exception {
        ftpClient.disconnect();
        super.tearDown();
    }

    public FTPClientConfigFunctionalTest(String arg0) {
        super(arg0);
    }

    private Set<FTPFile> getSortedList(FTPFile[] files) {
        // create a TreeSet which will sort each element
        // as it is added.
        Set<FTPFile> sorted = new TreeSet<>(Comparator.comparing(FTPFile::getTimestamp));
        for (FTPFile file : files) {
            // The directory contains a few additional files at the beginning
            // which aren't in the series we want. The series we want consists
            // of files named sn.dddd. This adjusts the file list to get rid
            // of the uninteresting ones.
            if (file.getName().startsWith("sn")) {
                sorted.add(file);
            }
        }
        return sorted;
    }

    public void testTimeZoneFunctionality() throws Exception {
        FTPFile[] files = ftpClient.listFiles();
        Set<FTPFile> sorted = getSortedList(files);
        //SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm z" );
        FTPFile lastfile = null;
        FTPFile firstfile = null;
        for (FTPFile thisfile : sorted) {
            if (firstfile == null) {
                firstfile = thisfile;
            }
            //System.out.println(sdf.format(thisfile.getTimestamp().getTime())
            //        + " " +thisfile.getName());
            if (lastfile != null) {
                // verify that the list is sorted earliest to latest.
                assertTrue(lastfile.getTimestamp()
                        .isBefore(thisfile.getTimestamp()));
            }
            lastfile = thisfile;
        }

        if (firstfile == null)  {
            fail("No files found");
        } else {
            // test that notwithstanding any time zone differences, the newest file
            // is older than now.
            assertTrue(lastfile.getTimestamp().isBefore(ZonedDateTime.now()));

            ZonedDateTime first = firstfile.getTimestamp();
            // test that the oldest is less than two days older than the newest
            // and, in particular, that no files have been considered "future"
            // by the parser and therefore been relegated to the same date a
            // year ago.
            first = first.plusDays(2);

            assertTrue(lastfile.getTimestamp() + " after "+ first,
                    lastfile.getTimestamp().isAfter(first));
        }
    }
}




