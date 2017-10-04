package org.xbib.io.ftp.client;

import java.io.IOException;

/**
 * FTPConnectionClosedException is used to indicate the premature or
 * unexpected closing of an FTP connection resulting from a
 * {@link FTPReply#SERVICE_NOT_AVAILABLE FTPReply.SERVICE_NOT_AVAILABLE }
 *  response (FTP reply code 421) to a
 * failed FTP command.  This exception is derived from IOException and
 * therefore may be caught either as an IOException or specifically as an
 * FTPConnectionClosedException.
 *
 * @see FTP
 * @see FTPClient
 */
public class ConnectionClosedException extends IOException {

    private static final long serialVersionUID = 3500547241659379952L;

    /***
     * Constructs a FTPConnectionClosedException with a specified message.
     *
     * @param message  The message explaining the reason for the exception.
     ***/
    public ConnectionClosedException(String message) {
        super(message);
    }

}
