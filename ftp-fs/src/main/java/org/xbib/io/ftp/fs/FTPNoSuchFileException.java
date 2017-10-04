package org.xbib.io.ftp.fs;

import java.nio.file.NoSuchFileException;

/**
 * An exception that is thrown if an FTP command does not execute successfully because a file does not exist.
 */
public class FTPNoSuchFileException extends NoSuchFileException implements FTPResponse {

    private static final long serialVersionUID = 1547360368371410860L;

    private final int replyCode;

    /**
     * Creates a new {@code FTPNoSuchFileException}.
     *
     * @param file        A string identifying the file, or {@code null} if not known.
     * @param replyCode   The integer value of the reply code of the last FTP reply that triggered this exception.
     * @param replyString The entire text from the last FTP response that triggered this exception. It will be used as the exception's reason.
     */
    public FTPNoSuchFileException(String file, int replyCode, String replyString) {
        super(file, null, replyString);
        this.replyCode = replyCode;
    }

    /**
     * Creates a new {@code FTPNoSuchFileException}.
     *
     * @param file        A string identifying the file, or {@code null} if not known.
     * @param other       A string identifying the other file, or {@code null} if not known.
     * @param replyCode   The integer value of the reply code of the last FTP reply that triggered this exception.
     * @param replyString The entire text from the last FTP response that triggered this exception. It will be used as the exception's reason.
     */
    public FTPNoSuchFileException(String file, String other, int replyCode, String replyString) {
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
