package org.xbib.io.ftp.client.parser;

import org.xbib.io.ftp.client.FTPClientConfig;
import org.xbib.io.ftp.client.FTPFile;
import org.xbib.io.ftp.client.FTPFileEntryParser;

import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Implementation of FTPFileEntryParser and FTPFileListParser for NT Systems.
 *
 * @see FTPFileEntryParser (for usage instructions)
 */
public class NTFTPEntryParser extends ConfigurableFTPFileEntryParserImpl {

    /**
     * this is the regular expression used by this parser.
     */
    private static final String REGEX =
            "(\\S+)\\s+(\\S+)\\s+"          // MM-dd-yy whitespace hh:mma|kk:mm; swallow trailing spaces
                    + "(?:(<DIR>)|([0-9]+))\\s+"    // <DIR> or ddddd; swallow trailing spaces
                    + "(\\S.*)";                    // First non-space followed by rest of line (name)
    private final FTPTimestampParser timestampParser;

    /**
     * The sole constructor for an NTFTPEntryParser object.
     *
     * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not be seen
     *                                  under normal conditions.  It it is seen, this is a sign that
     *                                  <code>REGEX</code> is  not a valid regular expression.
     */
    public NTFTPEntryParser() {
        this(null);
    }

    /**
     * This constructor allows the creation of an NTFTPEntryParser object
     * with something other than the default configuration.
     *
     * @param config The {@link FTPClientConfig configuration} object used to
     *               configure this parser.
     * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not be seen
     *                                  under normal conditions.  It it is seen, this is a sign that
     *                                  <code>REGEX</code> is  not a valid regular expression.
     */
    public NTFTPEntryParser(FTPClientConfig config) {
        super(REGEX, Pattern.DOTALL);
        configure(config);
        this.timestampParser = new ZonedDateTimeParser();
    }

    /**
     * Parses a line of an NT FTP server file listing and converts it into a
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
        f.setRawListing(entry);

        if (matches(entry)) {
            String datestr = group(1) + " " + group(2);
            String dirString = group(3);
            String size = group(4);
            String name = group(5);
            try {
                f.setTimestamp(super.parseTimestamp(datestr));
            } catch (DateTimeParseException e) {
                // parsing fails, try the other date format
                try {
                    f.setTimestamp(timestampParser.parseTimestamp(datestr));
                } catch (DateTimeParseException e2) {
                    // intentionally do nothing
                }
            }

            if (null == name || name.equals(".") || name.equals("..")) {
                return (null);
            }
            f.setName(name);


            if ("<DIR>".equals(dirString)) {
                f.setType(FTPFile.DIRECTORY_TYPE);
                f.setSize(0);
            } else {
                f.setType(FTPFile.FILE_TYPE);
                if (null != size) {
                    f.setSize(Long.parseLong(size));
                }
            }
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
    public FTPClientConfig getDefaultConfiguration() {
        return new FTPClientConfig(FTPClientConfig.SYST_NT, null, null);
    }

}
