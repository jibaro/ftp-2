package org.xbib.io.ftp.client.parser;

/**
 * This class encapsulates all errors that may be thrown by
 * the process of an FTPFileEntryParserFactory creating and
 * instantiating an FTPFileEntryParser.
 */
public class ParserInitializationException extends RuntimeException {

    private static final long serialVersionUID = 5563335279583210658L;

    /**
     * Constucts a ParserInitializationException with just a message
     *
     * @param message Exception message
     */
    public ParserInitializationException(String message) {
        super(message);
    }

    /**
     * Constucts a ParserInitializationException with a message
     * and a root cause.
     *
     * @param message   Exception message
     * @param rootCause root cause throwable that caused
     *                  this to be thrown
     */
    public ParserInitializationException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
}
