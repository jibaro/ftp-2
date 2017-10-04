package org.xbib.io.ftp.fs;

import java.nio.file.NotLinkException;

/**
 * An exception that is thrown if an FTP command does not execute successfully because a file is not a symbolic link.
 */
public class FTPNotLinkException extends NotLinkException implements FTPResponse {

    private static final long serialVersionUID = 2100528879214315190L;

    private final int replyCode;

    /**
     * Creates a new {@code FTPNotLinkException}.
     *
     * @param file        A string identifying the file, or {@code null} if not known.
     * @param replyCode   The integer value of the reply code of the last FTP reply that triggered this exception.
     * @param replyString The entire text from the last FTP response that triggered this exception. It will be used as the exception's reason.
     */
    public FTPNotLinkException(String file, int replyCode, String replyString) {
        super(file, null, replyString);
        this.replyCode = replyCode;
    }

    /**
     * Creates a new {@code FTPNotLinkException}.
     *
     * @param file        A string identifying the file, or {@code null} if not known.
     * @param other       A string identifying the other file, or {@code null} if not known.
     * @param replyCode   The integer value of the reply code of the last FTP reply that triggered this exception.
     * @param replyString The entire text from the last FTP response that triggered this exception. It will be used as the exception's reason.
     */
    public FTPNotLinkException(String file, String other, int replyCode, String replyString) {
        super(file, other, replyString);
        this.replyCode = replyCode;
    }

    @Override
    public int getReplyCode() {
        return replyCode;
    }

    @Override
    public String getReplyString() {
        return getReason();
    }
}
