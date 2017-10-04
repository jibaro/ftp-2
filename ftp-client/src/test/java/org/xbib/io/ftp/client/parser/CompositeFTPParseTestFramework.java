package org.xbib.io.ftp.client.parser;

import org.xbib.io.ftp.client.FTPFile;
import org.xbib.io.ftp.client.FTPFileEntryParser;

import java.time.format.DateTimeParseException;

/**
 */
public abstract class CompositeFTPParseTestFramework extends FTPParseTestFramework
{
    public CompositeFTPParseTestFramework(String name)
    {
        super(name);
    }

    @Override
    protected String[] getGoodListing()
    {
        return (getGoodListings()[0]);
    }

    /**
     * Method getBadListing.
     * Implementors must provide multiple listing that contains failures and
     * must force the composite parser to switch the FtpEntryParser
     *
     * @return String[]
     */
    protected abstract String[][] getBadListings();

    /**
     * Method getGoodListing.
     * Implementors must provide multiple listing that passes and
     * must force the composite parser to switch the FtpEntryParser
     *
     * @return String[]
     */
    protected abstract String[][] getGoodListings();

    @Override
    protected String[] getBadListing()
    {
        return (getBadListings()[0]);
    }

    public void testConsistentListing() throws Exception
    {
        String goodsamples[][] = getGoodListings();

        for (String[] goodsample : goodsamples)
        {
            FTPFileEntryParser parser = getParser();
            for (String test : goodsample) {
                FTPFile f = parser.parseFTPEntry(test);
                assertNotNull("Failed to parse " + test,
                        f);

                doAdditionalGoodTests(test, f);
            }
        }
    }

    @Override
    public void testBadListing() {
        String badsamples[][] = getBadListings();
        for (String[] badsample : badsamples) {
            FTPFileEntryParser parser = getParser();
            for (String test : badsample) {
                try {
                    FTPFile f = parser.parseFTPEntry(test);
                    assertNull("Should have Failed to parse " + test, nullFileOrNullDate(f));
                    doAdditionalBadTests(test, f);
                } catch (DateTimeParseException e) {
                }
            }
        }
    }

    // even though all these listings are good using one parser
    // or the other, this tests that a parser that has succeeded
    // on one format will fail if another format is substituted.
    public void testInconsistentListing() throws Exception {
        String goodsamples[][] = getGoodListings();
        FTPFileEntryParser parser = getParser();
        for (int i = 0; i < goodsamples.length; i++) {
            String test = goodsamples[i][0];
            FTPFile f = parser.parseFTPEntry(test);
            switch (i) {
            case 0:
                assertNotNull("Failed to parse " + test, f);
                break;
            case 1:
                assertNull("Should have failed to parse " + test, f);
                break;
            }
        }
    }
}
