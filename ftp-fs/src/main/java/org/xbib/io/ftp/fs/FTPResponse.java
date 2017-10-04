package org.xbib.io.ftp.fs;

/**
 * Represents a response from an FTP server.
 */
public interface FTPResponse {

    /**
     * Returns the reply code of the FTP response.
     *
     * @return The integer value of the reply code of the FTP response.
     */
    int getReplyCode();

    /**
     * Returns the entire text from the FTP response.
     *
     * @return The entire text from the FTP response.
     */
    String getReplyString();
}
