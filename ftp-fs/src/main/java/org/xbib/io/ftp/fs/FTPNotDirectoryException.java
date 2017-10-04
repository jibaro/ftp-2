package org.xbib.io.ftp.fs;

import java.nio.file.NotDirectoryException;

/**
 * An exception that is thrown if an FTP command does not execute successfully because a file is not a directory.
 */
public class FTPNotDirectoryException extends NotDirectoryException implements FTPResponse {

    private static final long serialVersionUID = -37768328123340304L;

    private final int replyCode;
    private final String replyString;

    /**
     * Creates a new {@code FTPNotLinkException}.
     *
     * @param file        A string identifying the file, or {@code null} if not known.
     * @param replyCode   The integer value of the reply code of the last FTP reply that triggered this exception.
     * @param replyString The entire text from the last FTP response that triggered this exception. It will be used as the exception's reason.
     */
    public FTPNotDirectoryException(String file, int replyCode, String replyString) {
        super(file);
        this.replyCode = replyCode;
        this.replyString = replyString;
    }

    @Override
    public int getReplyCode() {
        return replyCode;
    }

    @Override
    public String getReplyString() {
        return replyString;
    }

    @Override
    public String getReason() {
        return replyString;
    }

    @Override
    public String getMessage() {
        return replyString;
    }
}
