package org.xbib.io.ftp.fs;

import java.nio.file.FileAlreadyExistsException;

/**
 * An exception that is thrown if an FTP command does not execute successfully because a file already exists.
 */
public class FTPFileAlreadyExistsException extends FileAlreadyExistsException implements FTPResponse {

    private static final long serialVersionUID = 671724890729526141L;

    private final int replyCode;

    /**
     * Creates a new {@code FTPFileAlreadyExistsException}.
     *
     * @param file        A string identifying the file, or {@code null} if not known.
     * @param replyCode   The integer value of the reply code of the last FTP reply that triggered this exception.
     * @param replyString The entire text from the last FTP response that triggered this exception. It will be used as the exception's reason.
     */
    public FTPFileAlreadyExistsException(String file, int replyCode, String replyString) {
        super(file, null, replyString);
        this.replyCode = replyCode;
    }

    /**
     * Creates a new {@code FTPFileAlreadyExistsException}.
     *
     * @param file        A string identifying the file, or {@code null} if not known.
     * @param other       A string identifying the other file, or {@code null} if not known.
     * @param replyCode   The integer value of the reply code of the last FTP reply that triggered this exception.
     * @param replyString The entire text from the last FTP response that triggered this exception. It will be used as the exception's reason.
     */
    public FTPFileAlreadyExistsException(String file, String other, int replyCode, String replyString) {
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
