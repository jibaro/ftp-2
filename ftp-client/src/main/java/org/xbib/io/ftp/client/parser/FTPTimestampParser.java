package org.xbib.io.ftp.client.parser;

import java.time.ZonedDateTime;

/**
 * This interface specifies the concept of parsing an FTP server's timestamp.
 */
public interface FTPTimestampParser {

    String DEFAULT_DATE_FORMAT = "MMM d yyyy"; //Nov 9 2001

    String DEFAULT_RECENT_DATE_FORMAT = "MMM d HH:mm"; //Nov 9 20:06

    String NUMERIC_DATE_FORMAT = "yyyy-MM-dd HH:mm"; //2001-11-09 20:06


    /**
     * Parses the supplied date stamp parameter.  This parameter typically would
     * have been pulled from a longer FTP listing via the regular expression
     * mechanism.
     *
     * @param timestampStr - the timestamp portion of the FTP directory listing to be parsed
     * @return a {@link java.time.ZonedDateTime} object initialized to the date
     * parsed by the parser
     */
    ZonedDateTime parseTimestamp(String timestampStr);

}
