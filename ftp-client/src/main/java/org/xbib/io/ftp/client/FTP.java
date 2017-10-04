package org.xbib.io.ftp.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/***
 * FTP provides the basic the functionality necessary to implement your
 * own FTP client.  It extends {@link SocketClient} since
 * extending TelnetClient was causing unwanted behavior (like connections
 * that did not time out properly).
 * To derive the full benefits of the FTP class requires some knowledge
 * of the FTP protocol defined in RFC 959.  However, there is no reason
 * why you should have to use the FTP class.  The
 * {@link FTPClient} class,
 * derived from FTP,
 * implements all the functionality required of an FTP client.  The
 * FTP class is made public to provide access to various FTP constants
 * and to make it easier for adventurous programmers (or those with
 * special needs) to interact with the FTP protocol and implement their
 * own clients.  A set of methods with names corresponding to the FTP
 * command names are provided to facilitate this interaction.
 * <p>
 * You should keep in mind that the FTP server may choose to prematurely
 * close a connection if the client has been idle for longer than a
 * given time period (usually 900 seconds).  The FTP class will detect a
 * premature FTP server connection closing when it receives a
 * {@link FTPReply#SERVICE_NOT_AVAILABLE FTPReply.SERVICE_NOT_AVAILABLE }
 *  response to a command.
 * When that occurs, the FTP class method encountering that reply will throw
 * an {@link ConnectionClosedException}
 * .  <code>FTPConectionClosedException</code>
 * is a subclass of <code> IOException </code> and therefore need not be
 * caught separately, but if you are going to catch it separately, its
 * catch block must appear before the more general <code> IOException </code>
 * catch block.  When you encounter an
 * {@link ConnectionClosedException}
 * , you must disconnect the connection with
 * {@link #disconnect  disconnect() } to properly clean up the
 * system resources used by FTP.  Before disconnecting, you may check the
 * last reply code and text with
 * {@link #getReplyCode  getReplyCode },
 * {@link #getReplyString  getReplyString },
 * and {@link #getReplyStrings  getReplyStrings}.
 * You may avoid server disconnections while the client is idle by
 * periodicaly sending NOOP commands to the server.
 * <p>
 * Rather than list it separately for each method, we mention here that
 * every method communicating with the server and throwing an IOException
 * can also throw a
 * {@link MalformedServerReplyException}
 * , which is a subclass
 * of IOException.  A MalformedServerReplyException will be thrown when
 * the reply received from the server deviates enough from the protocol
 * specification that it cannot be interpreted in a useful manner despite
 * attempts to be as lenient as possible.
 *
 * @see FTPClient
 * @see ConnectionClosedException
 * @see MalformedServerReplyException
 ***/

public class FTP extends SocketClient {
    /*** The default FTP data port (20). ***/
    public static final int DEFAULT_DATA_PORT = 20;
    /*** The default FTP control port (21). ***/
    public static final int DEFAULT_PORT = 21;

    /***
     * A constant used to indicate the file(s) being transferred should
     * be treated as ASCII.  This is the default file type.  All constants
     * ending in <code>FILE_TYPE</code> are used to indicate file types.
     ***/
    public static final int ASCII_FILE_TYPE = 0;

    /***
     * A constant used to indicate the file(s) being transferred should
     * be treated as EBCDIC.  Note however that there are several different
     * EBCDIC formats.  All constants ending in <code>FILE_TYPE</code>
     * are used to indicate file types.
     ***/
    public static final int EBCDIC_FILE_TYPE = 1;


    /***
     * A constant used to indicate the file(s) being transferred should
     * be treated as a binary image, i.e., no translations should be
     * performed.  All constants ending in <code>FILE_TYPE</code> are used to
     * indicate file types.
     ***/
    public static final int BINARY_FILE_TYPE = 2;

    /***
     * A constant used to indicate the file(s) being transferred should
     * be treated as a local type.  All constants ending in
     * <code>FILE_TYPE</code> are used to indicate file types.
     ***/
    public static final int LOCAL_FILE_TYPE = 3;

    /***
     * A constant used for text files to indicate a non-print text format.
     * This is the default format.
     * All constants ending in <code>TEXT_FORMAT</code> are used to indicate
     * text formatting for text transfers (both ASCII and EBCDIC).
     ***/
    public static final int NON_PRINT_TEXT_FORMAT = 4;

    /***
     * A constant used to indicate a text file contains format vertical format
     * control characters.
     * All constants ending in <code>TEXT_FORMAT</code> are used to indicate
     * text formatting for text transfers (both ASCII and EBCDIC).
     ***/
    public static final int TELNET_TEXT_FORMAT = 5;

    /***
     * A constant used to indicate a text file contains ASA vertical format
     * control characters.
     * All constants ending in <code>TEXT_FORMAT</code> are used to indicate
     * text formatting for text transfers (both ASCII and EBCDIC).
     ***/
    public static final int CARRIAGE_CONTROL_TEXT_FORMAT = 6;

    /***
     * A constant used to indicate a file is to be treated as a continuous
     * sequence of bytes.  This is the default structure.  All constants ending
     * in <code>_STRUCTURE</code> are used to indicate file structure for
     * file transfers.
     ***/
    public static final int FILE_STRUCTURE = 7;

    /***
     * A constant used to indicate a file is to be treated as a sequence
     * of records.  All constants ending in <code>_STRUCTURE</code>
     * are used to indicate file structure for file transfers.
     ***/
    public static final int RECORD_STRUCTURE = 8;

    /***
     * A constant used to indicate a file is to be treated as a set of
     * independent indexed pages.  All constants ending in
     * <code>_STRUCTURE</code> are used to indicate file structure for file
     * transfers.
     ***/
    public static final int PAGE_STRUCTURE = 9;

    /***
     * A constant used to indicate a file is to be transferred as a stream
     * of bytes.  This is the default transfer mode.  All constants ending
     * in <code>TRANSFER_MODE</code> are used to indicate file transfer
     * modes.
     ***/
    public static final int STREAM_TRANSFER_MODE = 10;

    /***
     * A constant used to indicate a file is to be transferred as a series
     * of blocks.  All constants ending in <code>TRANSFER_MODE</code> are used
     * to indicate file transfer modes.
     ***/
    public static final int BLOCK_TRANSFER_MODE = 11;

    /***
     * A constant used to indicate a file is to be transferred as FTP
     * compressed data.  All constants ending in <code>TRANSFER_MODE</code>
     * are used to indicate file transfer modes.
     ***/
    public static final int COMPRESSED_TRANSFER_MODE = 12;

    // We have to ensure that the protocol communication is in ASCII
    // but we use ISO-8859-1 just in case 8-bit characters cross
    // the wire.
    /**
     * The default character encoding used for communicating over an
     * FTP control connection.  The default encoding is an
     * ASCII-compatible encoding.  Some FTP servers expect other
     * encodings.  You can change the encoding used by an FTP instance
     * with {@link #setControlEncoding setControlEncoding}.
     */
    public static final String DEFAULT_CONTROL_ENCODING = "ISO-8859-1";

    /**
     * Length of the FTP reply code (3 alphanumerics)
     */
    public static final int REPLY_CODE_LEN = 3;

    private static final String __modes = "AEILNTCFRPSBC";

    protected int replyCode;
    protected List<String> replyLines;
    protected boolean newReplyString;
    protected String replyString;
    protected String controlEncoding;

    /**
     * A ProtocolCommandSupport object used to manage the registering of
     * ProtocolCommandListeners and te firing of ProtocolCommandEvents.
     */
    protected ProtocolCommandSupport protocolCommandSupport;

    /**
     * This is used to signal whether a block of multiline responses beginning
     * with xxx must be terminated by the same numeric code xxx
     * See section 4.2 of RFC 959 for details.
     */
    protected boolean strictMultilineParsing = false;
    /**
     * Wraps SocketClient._input_ to facilitate the reading of text
     * from the FTP control connection.  Do not access the control
     * connection via SocketClient._input_.  This member starts
     * with a null value, is initialized in {@link #_connectAction_},
     * and set to null in {@link #disconnect}.
     */
    protected BufferedReader bufferedReader;
    /**
     * Wraps SocketClient._output_ to facilitate the writing of text
     * to the FTP control connection.  Do not access the control
     * connection via SocketClient._output_.  This member starts
     * with a null value, is initialized in {@link #_connectAction_},
     * and set to null in {@link #disconnect}.
     */
    protected BufferedWriter bufferedWriter;
    /**
     * If this is true, then non-multiline replies must have the format:
     * 3 digit code <space> <text>
     * If false, then the 3 digit code does not have to be followed by space
     * See section 4.2 of RFC 959 for details.
     */
    private boolean strictReplyParsing = true;

    /***
     * The default FTP constructor.  Sets the default port to
     * <code>DEFAULT_PORT</code> and initializes internal data structures
     * for saving FTP reply information.
     ***/
    public FTP() {
        super();
        setDefaultPort(DEFAULT_PORT);
        replyLines = new ArrayList<>();
        newReplyString = false;
        replyString = null;
        controlEncoding = DEFAULT_CONTROL_ENCODING;
        protocolCommandSupport = new ProtocolCommandSupport(this);
    }

    // The RFC-compliant multiline termination check
    private boolean strictCheck(String line, String code) {
        return (!(line.startsWith(code) && line.charAt(REPLY_CODE_LEN) == ' '));
    }

    // The strict check is too strong a condition because of non-conforming ftp
    // servers like ftp.funet.fi which sent 226 as the last line of a
    // 426 multi-line reply in response to ls /.  We relax the condition to
    // test that the line starts with a digit rather than starting with
    // the code.
    private boolean lenientCheck(String line) {
        return (!(line.length() > REPLY_CODE_LEN && line.charAt(REPLY_CODE_LEN) != '-' &&
                Character.isDigit(line.charAt(0))));
    }

    /**
     * Get the reply, and pass it to command listeners
     */
    private void internalGetReply() throws IOException {
        internalGetReply(true);
    }

    private void internalGetReply(boolean reportReply) throws IOException {
        int length;
        newReplyString = true;
        replyLines.clear();
        String line = bufferedReader.readLine();
        if (line == null) {
            throw new ConnectionClosedException("connection closed without indication");
        }
        // In case we run into an anomaly we don't want fatal index exceptions
        // to be thrown.
        length = line.length();
        if (length < REPLY_CODE_LEN) {
            throw new MalformedServerReplyException("Truncated server reply: " + line);
        }
        String code;
        try {
            code = line.substring(0, REPLY_CODE_LEN);
            replyCode = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            throw new MalformedServerReplyException("Could not parse response code.\nServer Reply: " + line);
        }
        replyLines.add(line);
        // Check the server reply type
        if (length > REPLY_CODE_LEN) {
            char sep = line.charAt(REPLY_CODE_LEN);
            // Get extra lines if message continues.
            if (sep == '-') {
                do {
                    line = bufferedReader.readLine();
                    if (line == null) {
                        throw new ConnectionClosedException("connection closed without indication");
                    }
                    replyLines.add(line);
                    // The length() check handles problems that could arise from readLine()
                    // returning too soon after encountering a naked CR or some other
                    // anomaly.
                }
                while (isStrictMultilineParsing() ? strictCheck(line, code) : lenientCheck(line));

            } else if (isStrictReplyParsing()) {
                if (length == REPLY_CODE_LEN + 1) { // expecting some text
                    throw new MalformedServerReplyException("Truncated server reply: '" + line + "'");
                } else if (sep != ' ') {
                    throw new MalformedServerReplyException("Invalid server reply: '" + line + "'");
                }
            }
        } else if (isStrictReplyParsing()) {
            throw new MalformedServerReplyException("Truncated server reply: '" + line + "'");
        }
        if (reportReply) {
            fireReplyReceived(replyCode, getReplyString());
        }
        if (replyCode == FTPReply.SERVICE_NOT_AVAILABLE) {
            throw new ConnectionClosedException("server closed connection");
        }
    }

    /**
     * Initiates control connections and gets initial reply.
     * Initializes {@link #bufferedReader} and {@link #bufferedWriter}.
     */
    @Override
    protected void _connectAction_() throws IOException {
        _connectAction_(null);
    }

    /**
     * Initiates control connections and gets initial reply.
     * Initializes {@link #bufferedReader} and {@link #bufferedWriter}.
     *
     * @param socketIsReader the reader to reuse (if non-null)
     * @throws IOException on error
     */
    protected void _connectAction_(Reader socketIsReader) throws IOException {
        super._connectAction_(); // sets up _input_ and _output_
        if (socketIsReader == null) {
            bufferedReader = new CRLFLineReader(new InputStreamReader(inputStream, getControlEncoding()));
        } else {
            bufferedReader = new CRLFLineReader(socketIsReader);
        }
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, getControlEncoding()));
        if (connectTimeout > 0) { // NET-385
            int original = socket.getSoTimeout();
            socket.setSoTimeout(connectTimeout);
            try {
                internalGetReply();
                // If we received code 120, we have to fetch completion reply.
                if (FTPReply.isPositivePreliminary(replyCode)) {
                    internalGetReply();
                }
            } catch (SocketTimeoutException e) {
                IOException ioe = new IOException("Timed out waiting for initial connect reply");
                ioe.initCause(e);
                throw ioe;
            } finally {
                socket.setSoTimeout(original);
            }
        } else {
            internalGetReply();
            // If we received code 120, we have to fetch completion reply.
            if (FTPReply.isPositivePreliminary(replyCode)) {
                internalGetReply();
            }
        }
    }

    /**
     * @return The character encoding used to communicate over the
     * control connection.
     */
    public String getControlEncoding() {
        return controlEncoding;
    }

    /**
     * Saves the character encoding to be used by the FTP control connection.
     * Some FTP servers require that commands be issued in a non-ASCII
     * encoding like UTF-8 so that filenames with multi-byte character
     * representations (e.g, Big 8) can be specified.
     * <p>
     * Please note that this has to be set before the connection is established.
     *
     * @param encoding The new character encoding for the control connection.
     */
    public void setControlEncoding(String encoding) {
        controlEncoding = encoding;
    }

    /***
     * Closes the control connection to the FTP server and sets to null
     * some internal data so that the memory may be reclaimed by the
     * garbage collector.  The reply text and code information from the
     * last command is voided so that the memory it used may be reclaimed.
     * Also sets {@link #bufferedReader} and {@link #bufferedWriter} to null.
     *
     * @throws IOException If an error occurs while disconnecting.
     ***/
    @Override
    public void disconnect() throws IOException {
        super.disconnect();
        bufferedReader = null;
        bufferedWriter = null;
        newReplyString = false;
        replyString = null;
    }

    /***
     * Sends an FTP command to the server, waits for a reply and returns the
     * numerical response code.  After invocation, for more detailed
     * information, the actual reply text can be accessed by calling
     * {@link #getReplyString  getReplyString } or
     * {@link #getReplyStrings  getReplyStrings }.
     *
     * @param command  The text representation of the  FTP command to send.
     * @param args The arguments to the FTP command.  If this parameter is
     *             set to null, then the command is sent with no argument.
     * @return The integer value of the FTP reply code returned by the server
     *         in response to the command.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int sendCommand(String command, String args) throws IOException {
        if (bufferedWriter == null) {
            throw new IOException("Connection is not open");
        }

        final String message = __buildMessage(command, args);

        __send(message);

        fireCommandSent(command, message);

        internalGetReply();
        return replyCode;
    }

    private String __buildMessage(String command, String args) {
        final StringBuilder __commandBuffer = new StringBuilder();

        __commandBuffer.append(command);

        if (args != null) {
            __commandBuffer.append(' ');
            __commandBuffer.append(args);
        }
        __commandBuffer.append("\r\n");
        return __commandBuffer.toString();
    }

    private void __send(String message) throws IOException {
        try {
            bufferedWriter.write(message);
            bufferedWriter.flush();
        } catch (SocketException e) {
            if (!isConnected()) {
                throw new IOException("connection unexpectedly closed");
            } else {
                throw e;
            }
        }
    }

    /**
     * Send a noop and get the reply without reporting to the command listener.
     * Intended for use with keep-alive.
     *
     * @throws IOException on error
     */
    protected void __noop() throws IOException {
        String msg = __buildMessage(FTPCmd.NOOP.getCommand(), null);
        __send(msg);
        internalGetReply(false);
    }

    /**
     * Sends an FTP command to the server, waits for a reply and returns the
     * numerical response code.  After invocation, for more detailed
     * information, the actual reply text can be accessed by calling
     * {@link #getReplyString  getReplyString } or
     * {@link #getReplyStrings  getReplyStrings }.
     *
     * @param command The FTPCmd enum corresponding to the FTP command
     *                to send.
     * @return The integer value of the FTP reply code returned by the server
     * in response to the command.
     * @throws ConnectionClosedException If the FTP server prematurely closes the connection as a result
     *                                   of the client being idle or some other reason causing the server
     *                                   to send FTP reply code 421.  This exception may be caught either
     *                                   as an IOException or independently as itself.
     * @throws IOException               If an I/O error occurs while either sending the
     *                                   command or receiving the server reply.
     */
    public int sendCommand(FTPCmd command) throws IOException {
        return sendCommand(command, null);
    }

    /**
     * Sends an FTP command to the server, waits for a reply and returns the
     * numerical response code.  After invocation, for more detailed
     * information, the actual reply text can be accessed by calling
     * {@link #getReplyString  getReplyString } or
     * {@link #getReplyStrings  getReplyStrings }.
     *
     * @param command The FTPCmd enum corresponding to the FTP command
     *                to send.
     * @param args    The arguments to the FTP command.  If this parameter is
     *                set to null, then the command is sent with no argument.
     * @return The integer value of the FTP reply code returned by the server
     * in response to the command.
     * @throws ConnectionClosedException If the FTP server prematurely closes the connection as a result
     *                                   of the client being idle or some other reason causing the server
     *                                   to send FTP reply code 421.  This exception may be caught either
     *                                   as an IOException or independently as itself.
     * @throws IOException               If an I/O error occurs while either sending the
     *                                   command or receiving the server reply.
     */
    public int sendCommand(FTPCmd command, String args) throws IOException {
        return sendCommand(command.getCommand(), args);
    }

    /***
     * Sends an FTP command with no arguments to the server, waits for a
     * reply and returns the numerical response code.  After invocation, for
     * more detailed information, the actual reply text can be accessed by
     * calling {@link #getReplyString  getReplyString } or
     * {@link #getReplyStrings  getReplyStrings }.
     *
     * @param command  The text representation of the  FTP command to send.
     * @return The integer value of the FTP reply code returned by the server
     *         in response to the command.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int sendCommand(String command) throws IOException {
        return sendCommand(command, null);
    }

    /***
     * Returns the integer value of the reply code of the last FTP reply.
     * You will usually only use this method after you connect to the
     * FTP server to check that the connection was successful since
     * <code> connect </code> is of type void.
     *
     * @return The integer value of the reply code of the last FTP reply.
     ***/
    public int getReplyCode() {
        return replyCode;
    }

    /***
     * Fetches a reply from the FTP server and returns the integer reply
     * code.  After calling this method, the actual reply text can be accessed
     * from either  calling {@link #getReplyString  getReplyString } or
     * {@link #getReplyStrings  getReplyStrings }.  Only use this
     * method if you are implementing your own FTP client or if you need to
     * fetch a secondary response from the FTP server.
     *
     * @return The integer value of the reply code of the fetched FTP reply.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while receiving the
     *                         server reply.
     ***/
    public int getReply() throws IOException {
        internalGetReply();
        return replyCode;
    }


    /***
     * Returns the lines of text from the last FTP server response as an array
     * of strings, one entry per line.  The end of line markers of each are
     * stripped from each line.
     *
     * @return The lines of text from the last FTP response as an array.
     ***/
    public String[] getReplyStrings() {
        return replyLines.toArray(new String[replyLines.size()]);
    }

    /***
     * Returns the entire text of the last FTP server response exactly
     * as it was received, including all end of line markers in NETASCII
     * format.
     *
     * @return The entire text from the last FTP response as a String.
     ***/
    public String getReplyString() {
        StringBuilder buffer;

        if (!newReplyString) {
            return replyString;
        }

        buffer = new StringBuilder(256);

        for (String line : replyLines) {
            buffer.append(line);
            buffer.append("\r\n");
        }

        newReplyString = false;

        return (replyString = buffer.toString());
    }


    /***
     * A convenience method to send the FTP USER command to the server,
     * receive the reply, and return the reply code.
     *
     * @param username  The username to login under.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int user(String username) throws IOException {
        return sendCommand(FTPCmd.USER, username);
    }

    /**
     * A convenience method to send the FTP PASS command to the server,
     * receive the reply, and return the reply code.
     *
     * @param password The plain text password of the username being logged into.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException If the FTP server prematurely closes the connection as a result
     *                                   of the client being idle or some other reason causing the server
     *                                   to send FTP reply code 421.  This exception may be caught either
     *                                   as an IOException or independently as itself.
     * @throws IOException               If an I/O error occurs while either sending the
     *                                   command or receiving the server reply.
     */
    public int pass(String password) throws IOException {
        return sendCommand(FTPCmd.PASS, password);
    }

    /***
     * A convenience method to send the FTP ACCT command to the server,
     * receive the reply, and return the reply code.
     *
     * @param account  The account name to access.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int acct(String account) throws IOException {
        return sendCommand(FTPCmd.ACCT, account);
    }


    /***
     * A convenience method to send the FTP ABOR command to the server,
     * receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int abor() throws IOException {
        return sendCommand(FTPCmd.ABOR);
    }

    /***
     * A convenience method to send the FTP CWD command to the server,
     * receive the reply, and return the reply code.
     *
     * @param directory The new working directory.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int cwd(String directory) throws IOException {
        return sendCommand(FTPCmd.CWD, directory);
    }

    /***
     * A convenience method to send the FTP CDUP command to the server,
     * receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int cdup() throws IOException {
        return sendCommand(FTPCmd.CDUP);
    }

    /***
     * A convenience method to send the FTP QUIT command to the server,
     * receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int quit() throws IOException {
        return sendCommand(FTPCmd.QUIT);
    }

    /***
     * A convenience method to send the FTP REIN command to the server,
     * receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int rein() throws IOException {
        return sendCommand(FTPCmd.REIN);
    }

    /***
     * A convenience method to send the FTP SMNT command to the server,
     * receive the reply, and return the reply code.
     *
     * @param dir  The directory name.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int smnt(String dir) throws IOException {
        return sendCommand(FTPCmd.SMNT, dir);
    }

    /***
     * A convenience method to send the FTP PORT command to the server,
     * receive the reply, and return the reply code.
     *
     * @param host  The host owning the port.
     * @param port  The new port.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int port(InetAddress host, int port) throws IOException {
        int num;
        StringBuilder info = new StringBuilder(24);

        info.append(host.getHostAddress().replace('.', ','));
        num = port >>> 8;
        info.append(',');
        info.append(num);
        info.append(',');
        num = port & 0xff;
        info.append(num);

        return sendCommand(FTPCmd.PORT, info.toString());
    }

    /***
     * A convenience method to send the FTP EPRT command to the server,
     * receive the reply, and return the reply code.
     *
     * Examples:
     * <ul>
     * <li>EPRT |1|132.235.1.2|6275|</li>
     * <li>EPRT |2|1080::8:800:200C:417A|5282|</li>
     * </ul>
     *
     * @see "http://www.faqs.org/rfcs/rfc2428.html"
     *
     * @param host  The host owning the port.
     * @param port  The new port.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int eprt(InetAddress host, int port) throws IOException {
        int num;
        StringBuilder info = new StringBuilder();
        String h;

        // If IPv6, trim the zone index
        h = host.getHostAddress();
        num = h.indexOf('%');
        if (num > 0) {
            h = h.substring(0, num);
        }

        info.append("|");

        if (host instanceof Inet4Address) {
            info.append("1");
        } else if (host instanceof Inet6Address) {
            info.append("2");
        }
        info.append("|");
        info.append(h);
        info.append("|");
        info.append(port);
        info.append("|");

        return sendCommand(FTPCmd.EPRT, info.toString());
    }

    /***
     * A convenience method to send the FTP PASV command to the server,
     * receive the reply, and return the reply code.  Remember, it's up
     * to you to interpret the reply string containing the host/port
     * information.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int pasv() throws IOException {
        return sendCommand(FTPCmd.PASV);
    }

    /***
     * A convenience method to send the FTP EPSV command to the server,
     * receive the reply, and return the reply code.  Remember, it's up
     * to you to interpret the reply string containing the host/port
     * information.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int epsv() throws IOException {
        return sendCommand(FTPCmd.EPSV);
    }

    /**
     * A convenience method to send the FTP TYPE command for text files
     * to the server, receive the reply, and return the reply code.
     *
     * @param fileType         The type of the file (one of the <code>FILE_TYPE</code>
     *                         constants).
     * @param formatOrByteSize The format of the file (one of the
     *                         <code>_FORMAT</code> constants.  In the case of
     *                         <code>LOCAL_FILE_TYPE</code>, the byte size.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException If the FTP server prematurely closes the connection as a result
     *                                   of the client being idle or some other reason causing the server
     *                                   to send FTP reply code 421.  This exception may be caught either
     *                                   as an IOException or independently as itself.
     * @throws IOException               If an I/O error occurs while either sending the
     *                                   command or receiving the server reply.
     */
    public int type(int fileType, int formatOrByteSize) throws IOException {
        StringBuilder arg = new StringBuilder();

        arg.append(__modes.charAt(fileType));
        arg.append(' ');
        if (fileType == LOCAL_FILE_TYPE) {
            arg.append(formatOrByteSize);
        } else {
            arg.append(__modes.charAt(formatOrByteSize));
        }

        return sendCommand(FTPCmd.TYPE, arg.toString());
    }


    /**
     * A convenience method to send the FTP TYPE command to the server,
     * receive the reply, and return the reply code.
     *
     * @param fileType The type of the file (one of the <code>FILE_TYPE</code>
     *                 constants).
     * @return The reply code received from the server.
     * @throws ConnectionClosedException If the FTP server prematurely closes the connection as a result
     *                                   of the client being idle or some other reason causing the server
     *                                   to send FTP reply code 421.  This exception may be caught either
     *                                   as an IOException or independently as itself.
     * @throws IOException               If an I/O error occurs while either sending the
     *                                   command or receiving the server reply.
     */
    public int type(int fileType) throws IOException {
        return sendCommand(FTPCmd.TYPE,
                __modes.substring(fileType, fileType + 1));
    }

    /***
     * A convenience method to send the FTP STRU command to the server,
     * receive the reply, and return the reply code.
     *
     * @param structure  The structure of the file (one of the
     *         <code>_STRUCTURE</code> constants).
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int stru(int structure) throws IOException {
        return sendCommand(FTPCmd.STRU,
                __modes.substring(structure, structure + 1));
    }

    /***
     * A convenience method to send the FTP MODE command to the server,
     * receive the reply, and return the reply code.
     *
     * @param mode  The transfer mode to use (one of the
     *         <code>TRANSFER_MODE</code> constants).
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int mode(int mode) throws IOException {
        return sendCommand(FTPCmd.MODE,
                __modes.substring(mode, mode + 1));
    }

    /***
     * A convenience method to send the FTP RETR command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @param pathname  The pathname of the file to retrieve.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int retr(String pathname) throws IOException {
        return sendCommand(FTPCmd.RETR, pathname);
    }

    /***
     * A convenience method to send the FTP STOR command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @param pathname  The pathname to use for the file when stored at
     *                  the remote end of the transfer.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int stor(String pathname) throws IOException {
        return sendCommand(FTPCmd.STOR, pathname);
    }

    /***
     * A convenience method to send the FTP STOU command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int stou() throws IOException {
        return sendCommand(FTPCmd.STOU);
    }

    /***
     * A convenience method to send the FTP STOU command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     * @param pathname  The base pathname to use for the file when stored at
     *                  the remote end of the transfer.  Some FTP servers
     *                  require this.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     */
    public int stou(String pathname) throws IOException {
        return sendCommand(FTPCmd.STOU, pathname);
    }

    /***
     * A convenience method to send the FTP APPE command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @param pathname  The pathname to use for the file when stored at
     *                  the remote end of the transfer.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int appe(String pathname) throws IOException {
        return sendCommand(FTPCmd.APPE, pathname);
    }

    /***
     * A convenience method to send the FTP ALLO command to the server,
     * receive the reply, and return the reply code.
     *
     * @param bytes The number of bytes to allocate.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int allo(int bytes) throws IOException {
        return sendCommand(FTPCmd.ALLO, Integer.toString(bytes));
    }

    /**
     * A convenience method to send the FTP FEAT command to the server, receive the reply,
     * and return the reply code.
     *
     * @return The reply code received by the server
     * @throws IOException If an I/O error occurs while either sending the
     *                     command or receiving the server reply.
     */
    public int feat() throws IOException {
        return sendCommand(FTPCmd.FEAT);
    }

    /***
     * A convenience method to send the FTP ALLO command to the server,
     * receive the reply, and return the reply code.
     *
     * @param bytes The number of bytes to allocate.
     * @param recordSize  The size of a record.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int allo(int bytes, int recordSize) throws IOException {
        return sendCommand(FTPCmd.ALLO, Integer.toString(bytes) + " R " +
                Integer.toString(recordSize));
    }

    /***
     * A convenience method to send the FTP REST command to the server,
     * receive the reply, and return the reply code.
     *
     * @param marker The marker at which to restart a transfer.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int rest(String marker) throws IOException {
        return sendCommand(FTPCmd.REST, marker);
    }


    /**
     * @param file name of file
     * @return the status
     * @throws IOException on error
     **/
    public int mdtm(String file) throws IOException {
        return sendCommand(FTPCmd.MDTM, file);
    }


    /**
     * A convenience method to send the FTP MFMT command to the server,
     * receive the reply, and return the reply code.
     *
     * @param pathname The pathname for which mtime is to be changed
     * @param timeval  Timestamp in <code>YYYYMMDDhhmmss</code> format
     * @return The reply code received from the server.
     * @throws ConnectionClosedException If the FTP server prematurely closes the connection as a result
     *                                   of the client being idle or some other reason causing the server
     *                                   to send FTP reply code 421.  This exception may be caught either
     *                                   as an IOException or independently as itself.
     * @throws IOException               If an I/O error occurs while either sending the
     *                                   command or receiving the server reply.
     * @see <a href="http://tools.ietf.org/html/draft-somers-ftp-mfxx-04">http://tools.ietf.org/html/draft-somers-ftp-mfxx-04</a>
     **/
    public int mfmt(String pathname, String timeval) throws IOException {
        return sendCommand(FTPCmd.MFMT, timeval + " " + pathname);
    }


    /***
     * A convenience method to send the FTP RNFR command to the server,
     * receive the reply, and return the reply code.
     *
     * @param pathname The pathname to rename from.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int rnfr(String pathname) throws IOException {
        return sendCommand(FTPCmd.RNFR, pathname);
    }

    /***
     * A convenience method to send the FTP RNTO command to the server,
     * receive the reply, and return the reply code.
     *
     * @param pathname The pathname to rename to
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int rnto(String pathname) throws IOException {
        return sendCommand(FTPCmd.RNTO, pathname);
    }

    /***
     * A convenience method to send the FTP DELE command to the server,
     * receive the reply, and return the reply code.
     *
     * @param pathname The pathname to delete.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int dele(String pathname) throws IOException {
        return sendCommand(FTPCmd.DELE, pathname);
    }

    /***
     * A convenience method to send the FTP RMD command to the server,
     * receive the reply, and return the reply code.
     *
     * @param pathname The pathname of the directory to remove.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int rmd(String pathname) throws IOException {
        return sendCommand(FTPCmd.RMD, pathname);
    }

    /***
     * A convenience method to send the FTP MKD command to the server,
     * receive the reply, and return the reply code.
     *
     * @param pathname The pathname of the new directory to create.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int mkd(String pathname) throws IOException {
        return sendCommand(FTPCmd.MKD, pathname);
    }

    /***
     * A convenience method to send the FTP PWD command to the server,
     * receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int pwd() throws IOException {
        return sendCommand(FTPCmd.PWD);
    }

    /***
     * A convenience method to send the FTP LIST command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int list() throws IOException {
        return sendCommand(FTPCmd.LIST);
    }

    /***
     * A convenience method to send the FTP LIST command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @param pathname  The pathname to list,
     * may be {@code null} in which case the command is sent with no parameters
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int list(String pathname) throws IOException {
        return sendCommand(FTPCmd.LIST, pathname);
    }

    /**
     * A convenience method to send the FTP MLSD command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException If the FTP server prematurely closes the connection as a result
     *                                   of the client being idle or some other reason causing the server
     *                                   to send FTP reply code 421.  This exception may be caught either
     *                                   as an IOException or independently as itself.
     * @throws IOException               If an I/O error occurs while either sending the
     *                                   command or receiving the server reply.
     */
    public int mlsd() throws IOException {
        return sendCommand(FTPCmd.MLSD);
    }

    /**
     * A convenience method to send the FTP MLSD command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @param path the path to report on
     * @return The reply code received from the server,
     * may be {@code null} in which case the command is sent with no parameters
     * @throws ConnectionClosedException If the FTP server prematurely closes the connection as a result
     *                                   of the client being idle or some other reason causing the server
     *                                   to send FTP reply code 421.  This exception may be caught either
     *                                   as an IOException or independently as itself.
     * @throws IOException               If an I/O error occurs while either sending the
     *                                   command or receiving the server reply.
     */
    public int mlsd(String path) throws IOException {
        return sendCommand(FTPCmd.MLSD, path);
    }

    /**
     * A convenience method to send the FTP MLST command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException If the FTP server prematurely closes the connection as a result
     *                                   of the client being idle or some other reason causing the server
     *                                   to send FTP reply code 421.  This exception may be caught either
     *                                   as an IOException or independently as itself.
     * @throws IOException               If an I/O error occurs while either sending the
     *                                   command or receiving the server reply.
     */
    public int mlst() throws IOException {
        return sendCommand(FTPCmd.MLST);
    }

    /**
     * A convenience method to send the FTP MLST command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @param path the path to report on
     * @return The reply code received from the server,
     * may be {@code null} in which case the command is sent with no parameters
     * @throws ConnectionClosedException If the FTP server prematurely closes the connection as a result
     *                                   of the client being idle or some other reason causing the server
     *                                   to send FTP reply code 421.  This exception may be caught either
     *                                   as an IOException or independently as itself.
     * @throws IOException               If an I/O error occurs while either sending the
     *                                   command or receiving the server reply.
     */
    public int mlst(String path) throws IOException {
        return sendCommand(FTPCmd.MLST, path);
    }

    /***
     * A convenience method to send the FTP NLST command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int nlst() throws IOException {
        return sendCommand(FTPCmd.NLST);
    }

    /***
     * A convenience method to send the FTP NLST command to the server,
     * receive the reply, and return the reply code.  Remember, it is up
     * to you to manage the data connection.  If you don't need this low
     * level of access, use {@link FTPClient}
     * , which will handle all low level details for you.
     *
     * @param pathname  The pathname to list,
     * may be {@code null} in which case the command is sent with no parameters
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int nlst(String pathname) throws IOException {
        return sendCommand(FTPCmd.NLST, pathname);
    }

    /***
     * A convenience method to send the FTP SITE command to the server,
     * receive the reply, and return the reply code.
     *
     * @param parameters  The site parameters to send.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int site(String parameters) throws IOException {
        return sendCommand(FTPCmd.SITE, parameters);
    }

    /***
     * A convenience method to send the FTP SIZE command to the server,
     * receive the reply, and return the reply code.
     *
     * @param parameters  The site parameters to send.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int size(String parameters) throws IOException {
        return sendCommand(FTPCmd.SIZE, parameters);
    }

    /***
     * A convenience method to send the FTP SYST command to the server,
     * receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int syst() throws IOException {
        return sendCommand(FTPCmd.SYST);
    }

    /***
     * A convenience method to send the FTP STAT command to the server,
     * receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int stat() throws IOException {
        return sendCommand(FTPCmd.STAT);
    }

    /***
     * A convenience method to send the FTP STAT command to the server,
     * receive the reply, and return the reply code.
     *
     * @param pathname  A pathname to list.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int stat(String pathname) throws IOException {
        return sendCommand(FTPCmd.STAT, pathname);
    }

    /***
     * A convenience method to send the FTP HELP command to the server,
     * receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int help() throws IOException {
        return sendCommand(FTPCmd.HELP);
    }

    /***
     * A convenience method to send the FTP HELP command to the server,
     * receive the reply, and return the reply code.
     *
     * @param command  The command name on which to request help.
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int help(String command) throws IOException {
        return sendCommand(FTPCmd.HELP, command);
    }

    /***
     * A convenience method to send the FTP NOOP command to the server,
     * receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws ConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @throws IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int noop() throws IOException {
        return sendCommand(FTPCmd.NOOP);
    }

    /**
     * Return whether strict multiline parsing is enabled, as per RFC 959, section 4.2.
     *
     * @return True if strict, false if lenient
     */
    public boolean isStrictMultilineParsing() {
        return strictMultilineParsing;
    }

    /**
     * Set strict multiline parsing.
     *
     * @param strictMultilineParsing the setting
     */
    public void setStrictMultilineParsing(boolean strictMultilineParsing) {
        this.strictMultilineParsing = strictMultilineParsing;
    }

    /**
     * Return whether strict non-multiline parsing is enabled, as per RFC 959, section 4.2.
     * The default is true, which requires the 3 digit code be followed by space and some text.
     * If false, only the 3 digit code is required (as was the case for versions up to 3.5)
     *
     * @return True if strict (default), false if additional checks are not made
     */
    public boolean isStrictReplyParsing() {
        return strictReplyParsing;
    }

    /**
     * Set strict non-multiline parsing.
     * If true, it requires the 3 digit code be followed by space and some text.
     * If false, only the 3 digit code is required (as was the case for versions up to 3.5)
     * <b>This should not be required by a well-behaved FTP server</b>
     *
     * @param strictReplyParsing the setting
     */
    public void setStrictReplyParsing(boolean strictReplyParsing) {
        this.strictReplyParsing = strictReplyParsing;
    }

    /**
     * Provide command support to super-class
     */
    @Override
    protected ProtocolCommandSupport getCommandSupport() {
        return protocolCommandSupport;
    }
}