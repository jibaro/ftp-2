package org.xbib.io.ftp.client.parser;

import org.xbib.io.ftp.client.FTPClientConfig;
import org.xbib.io.ftp.client.FTPFile;
import org.xbib.io.ftp.client.FTPFileEntryParser;

import java.time.format.DateTimeParseException;


/**
 * Implementation of FTPFileEntryParser and FTPFileListParser for OS2 Systems.
 *
 * @see FTPFileEntryParser (for usage instructions)
 */
public class OS2FTPEntryParser extends ConfigurableFTPFileEntryParserImpl {
    /**
     * this is the regular expression used by this parser.
     */
    private static final String REGEX =
            "\\s*([0-9]+)\\s*"
                    + "(\\s+|[A-Z]+)\\s*"
                    + "(DIR|\\s+)\\s*"
                    + "(\\S+)\\s+(\\S+)\\s+" /* date stuff */
                    + "(\\S.*)";

    /**
     * The default constructor for a OS2FTPEntryParser object.
     *
     * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not be seen
     *                                  under normal conditions.  It it is seen, this is a sign that
     *                                  <code>REGEX</code> is  not a valid regular expression.
     */
    public OS2FTPEntryParser() {
        this(null);
    }

    /**
     * This constructor allows the creation of an OS2FTPEntryParser object
     * with something other than the default configuration.
     *
     * @param config The {@link FTPClientConfig configuration} object used to
     *               configure this parser.
     * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not be seen
     *                                  under normal conditions.  It it is seen, this is a sign that
     *                                  <code>REGEX</code> is  not a valid regular expression.
     */
    public OS2FTPEntryParser(FTPClientConfig config) {
        super(REGEX);
        configure(config);
    }

    /**
     * Parses a line of an OS2 FTP server file listing and converts it into a
     * usable format in the form of an <code> FTPFile </code> instance.  If the
     * file listing line doesn't describe a file, <code> null </code> is
     * returned, otherwise a <code> FTPFile </code> instance representing the
     * files in the directory is returned.
     *
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    @Override
    public FTPFile parseFTPEntry(String entry) {

        FTPFile f = new FTPFile();
        if (matches(entry)) {
            String size = group(1);
            String attrib = group(2);
            String dirString = group(3);
            String datestr = group(4) + " " + group(5);
            String name = group(6);
            try {
                f.setTimestamp(super.parseTimestamp(datestr));
            } catch (DateTimeParseException e) {
                // intentionally do nothing
            }


            //is it a DIR or a file
            if (dirString.trim().equals("DIR") || attrib.trim().equals("DIR")) {
                f.setType(FTPFile.DIRECTORY_TYPE);
            } else {
                f.setType(FTPFile.FILE_TYPE);
            }


            //set the name
            f.setName(name.trim());

            //set the size
            f.setSize(Long.parseLong(size.trim()));

            return (f);
        }
        return null;

    }

    /**
     * Defines a default configuration to be used when this class is
     * instantiated without a {@link  FTPClientConfig  FTPClientConfig}
     * parameter being specified.
     *
     * @return the default configuration for this parser.
     */
    @Override
    protected FTPClientConfig getDefaultConfiguration() {
        return new FTPClientConfig(FTPClientConfig.SYST_OS2, null, null);
    }

}
