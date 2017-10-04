package org.xbib.io.ftp.client.parser;

import org.xbib.io.ftp.client.FTPFile;
import org.xbib.io.ftp.client.FTPFileEntryParser;

import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Parser for the Connect Enterprise Unix FTP Server From Sterling Commerce.
 * Here is a sample of the sort of output line this parser processes:
 * "-C--E-----FTP B QUA1I1      18128       41 Aug 12 13:56 QUADTEST"
 * <P><B>
 * Note: EnterpriseUnixFTPEntryParser can only be instantiated through the
 * DefaultFTPParserFactory by classname.  It will not be chosen
 * by the autodetection scheme.
 * </B>
 *
 * @see FTPFileEntryParser (for usage instructions)
 * @see DefaultFTPFileEntryParserFactory
 */
public class EnterpriseUnixFTPEntryParser extends RegexFTPFileEntryParserImpl {

    /**
     * months abbreviations looked for by this parser.  Also used
     * to determine <b>which</b> month has been matched by the parser.
     */
    private static final String MONTHS =
            "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";

    /**
     * this is the regular expression used by this parser.
     */
    private static final String REGEX =
            "(([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])"
                    + "([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z]))"
                    + "(\\S*)\\s*" // 12
                    + "(\\S+)\\s*" // 13
                    + "(\\S*)\\s*" // 14 user
                    + "(\\d*)\\s*" // 15 group
                    + "(\\d*)\\s*" // 16 filesize
                    + MONTHS       // 17 month
                    + "\\s*"       // TODO should the space be optional?
                    // TODO \\d* should be \\d? surely ? Otherwise 01111 is allowed
                    + "((?:[012]\\d*)|(?:3[01]))\\s*" // 18 date [012]\d* or 3[01]
                    + "((\\d\\d\\d\\d)|((?:[01]\\d)|(?:2[0123])):([012345]\\d))\\s"
                    // 20 \d\d\d\d  = year  OR
                    // 21 [01]\d or 2[0123] hour + ':'
                    // 22 [012345]\d = minute
                    + "(\\S*)(\\s*.*)"; // 23 name

    /**
     * The sole constructor for a EnterpriseUnixFTPEntryParser object.
     */
    public EnterpriseUnixFTPEntryParser() {
        super(REGEX);
    }

    /**
     * Parses a line of a unix FTP server file listing and converts  it into a
     * usable format in the form of an <code> FTPFile </code>  instance.  If
     * the file listing line doesn't describe a file,  <code> null </code> is
     * returned, otherwise a <code> FTPFile </code>  instance representing the
     * files in the directory is returned.
     *
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    @Override
    public FTPFile parseFTPEntry(String entry) {
        FTPFile file = new FTPFile();
        file.setRawListing(entry);
        if (matches(entry)) {
            String usr = group(14);
            String grp = group(15);
            String filesize = group(16);
            String mo = group(17);
            String da = group(18);
            String yr = group(20);
            String hr = group(21);
            String min = group(22);
            String name = group(23);
            file.setType(FTPFile.FILE_TYPE);
            file.setUser(usr);
            file.setGroup(grp);
            try {
                file.setSize(Long.parseLong(filesize));
            } catch (NumberFormatException e) {
                // intentionally do nothing
            }
            try {
                if (mo != null && da != null && hr != null && min != null) {
                    Year year = Year.now();
                    int pos = MONTHS.indexOf(mo);
                    int month = pos / 4;
                    if (yr != null) {
                        year = Year.of(Integer.parseInt(yr));
                    } else {
                        if (month > Month.from(ZonedDateTime.now()).getValue()) {
                            year = year.minusYears(1L);
                        }
                    }
                    int hour = Integer.parseInt(hr);
                    int minutes = Integer.parseInt(min);
                    int dayOfMonth = Integer.parseInt(da);
                    ZonedDateTime zonedDateTime =
                            ZonedDateTime.of(year.getValue(), month + 1, dayOfMonth, hour, minutes, 0, 0, ZoneId.of("UTC"));
                    file.setTimestamp(zonedDateTime);
                } else if (mo != null && da != null && yr != null) {
                    int dayOfMonth = Integer.parseInt(da);
                    int pos = MONTHS.indexOf(mo);
                    int month = pos / 4;
                    int year = Integer.parseInt(yr);
                    ZonedDateTime zonedDateTime =
                            ZonedDateTime.of(year, month + 1, dayOfMonth, 0, 0, 0, 0, ZoneId.of("UTC"));
                    file.setTimestamp(zonedDateTime);
                }
            } catch (NumberFormatException e) {
                // do nothing, date will be uninitialized
            }
            file.setName(name);
            return file;
        }
        return null;
    }
}
