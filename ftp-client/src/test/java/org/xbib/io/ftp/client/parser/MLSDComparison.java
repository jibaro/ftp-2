package org.xbib.io.ftp.client.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;

import org.junit.Test;
import org.xbib.io.ftp.client.FTP;
import org.xbib.io.ftp.client.FTPClientConfig;
import org.xbib.io.ftp.client.FTPFile;
import org.xbib.io.ftp.client.FTPFileFilters;
import org.xbib.io.ftp.client.FTPListParseEngine;

/**
 * Attempt comparison of LIST and MLSD listings
 */
public class MLSDComparison {

    static final String DOWNLOAD_DIR = "build/ftptest";

    private final Comparator<FTPFile> cmp = (o1, o2) -> {
            String n1 = o1.getName();
            String n2 = o2.getName();
            return n1.compareTo(n2);
        };

    @Test
    public void testFile() throws Exception{
        File path = new File(DOWNLOAD_DIR);
        FilenameFilter filter = (dir, name) -> name.endsWith("_mlsd.txt");
        File[] files = path.listFiles(filter);
        if (files == null) {
            return;
        }
        for (File mlsd :files ){
            //System.out.println(mlsd);
            InputStream is = new FileInputStream(mlsd);
            FTPListParseEngine engine = new FTPListParseEngine(MLSxEntryParser.getInstance());
            engine.readServerList(is, FTP.DEFAULT_CONTROL_ENCODING);
            FTPFile [] mlsds = engine.getFiles(FTPFileFilters.ALL);
            is.close();
            File list = new File(mlsd.getParentFile(),mlsd.getName().replace("_mlsd", "_list"));
            //System.out.println(list);
            is = new FileInputStream(list);
            FTPClientConfig cfg = new FTPClientConfig();
            cfg.setServerTimeZoneId("GMT");
            UnixFTPEntryParser parser = new UnixFTPEntryParser(cfg);
            engine = new FTPListParseEngine(parser);
            engine.readServerList(is, FTP.DEFAULT_CONTROL_ENCODING);
            FTPFile [] lists = engine.getFiles(FTPFileFilters.ALL);
            is.close();
            compareSortedLists(mlsds, lists);
        }
    }

    private void compareSortedLists(FTPFile[] lst, FTPFile[] mlst){
        Arrays.sort(lst, cmp );
        Arrays.sort(mlst, cmp );
        FTPFile first, second;
        int firstl=lst.length;
        int secondl=mlst.length;
        int one=0, two=0;
        first = lst[one++];
        second = mlst[two++];
        int cmp;
        while (one < firstl || two < secondl) {
//            String fs1 = first.toFormattedString();
//            String fs2 = second.toFormattedString();
            String rl1 = first.getRawListing();
            String rl2 = second.getRawListing();
            cmp = first.getName().compareTo(second.getName());
            if (cmp == 0) {
                if (first.getName().endsWith("HEADER.html")){
                    cmp = 0;
                }
                if (!areEquivalent(first, second)){
//                    System.out.println(rl1);
//                    System.out.println(fs1);
                    long tdiff = first.getTimestamp().toEpochSecond() - second.getTimestamp().toEpochSecond();
                    //System.out.println("Minutes diff "+tdiff/(1000*60));
//                    System.out.println(fs2);
//                    System.out.println(rl2);
//                    System.out.println();
//                    fail();
                }
                if (one < firstl) {
                    first = lst[one++];
                }
                if (two < secondl) {
                    second = mlst[two++];
                }
            } else if (cmp < 0) {
                if (!first.getName().startsWith(".")) { // skip hidden files
                    System.out.println("1: "+rl1);
                }
                if (one < firstl) {
                    first = lst[one++];
                }
            } else {
                System.out.println("2: "+rl2);
                if (two < secondl) {
                    second = mlst[two++];
                }
            }
        }
    }
    /**
     * Compare two instances to see if they are the same,
     * ignoring any uninitialised fields.
     * @param a first instance
     * @param b second instance
     * @return true if the initialised fields are the same
     */
    public boolean areEquivalent(FTPFile a, FTPFile b) {
        return
            a.getName().equals(b.getName()) &&
            areSame(a.getSize(), b.getSize(), -1L) &&
//            areSame(a.getUser(), b.getUser()) &&
//            areSame(a.getGroup(), b.getGroup()) &&
            areSame(a.getTimestamp(), b.getTimestamp()) &&
//            areSame(a.getType(), b.getType(), UNKNOWN_TYPE) &&
//            areSame(a.getHardLinkCount(), b.getHardLinkCount(), 0) &&
//            areSame(a._permissions, b._permissions)
            true
            ;
    }

    // compare permissions: default is all false, but that is also a possible
    // state, so this may miss some differences
//    private boolean areSame(boolean[][] a, boolean[][] b) {
//        return isDefault(a) || isDefault(b) || Arrays.deepEquals(a, b);
//    }

    // Is the array in its default state?
//    private boolean isDefault(boolean[][] a) {
//        for(boolean[] r : a){
//            for(boolean rc : r){
//                if (rc) { // not default
//                    return false;
//                }
//            }
//        }
//        return true;
//    }


    private boolean areSame(ZonedDateTime a, ZonedDateTime b) {
        return a.equals(b);
    }

    private boolean areSame(long a, long b, long d) {
        return a == d || b == d || a == b;
    }

//    private boolean areSame(int a, int b, int d) {
//        return a == d || b == d || a == b;
//    }
//
//    private boolean areSame(String a, String b) {
//        return a.length() == 0 || b.length() == 0 || a.equals(b);
//    }
}
