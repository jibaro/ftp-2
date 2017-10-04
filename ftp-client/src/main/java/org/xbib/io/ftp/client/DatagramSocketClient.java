package org.xbib.io.ftp.client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

/**
 * The DatagramSocketClient provides the basic operations that are required
 * of client objects accessing datagram sockets.  It is meant to be
 * subclassed to avoid having to rewrite the same code over and over again
 * to open a socket, close a socket, set timeouts, etc.  Of special note
 * is the {@link #setDatagramSocketFactory  setDatagramSocketFactory }
 * method, which allows you to control the type of DatagramSocket the
 * DatagramSocketClient creates for network communications.  This is
 * especially useful for adding things like proxy support as well as better
 * support for applets.  For
 * example, you could create a
 * {@link DatagramSocketFactory}
 *  that
 * requests browser security capabilities before creating a socket.
 * All classes derived from DatagramSocketClient should use the
 * {@link #datagramSocketFactory  _socketFactory_ } member variable to
 * create DatagramSocket instances rather than instantiating
 * them by directly invoking a constructor.  By honoring this contract
 * you guarantee that a user will always be able to provide his own
 * Socket implementations by substituting his own SocketFactory.
 *
 */
public abstract class DatagramSocketClient {
    /**
     * The default DatagramSocketFactory shared by all DatagramSocketClient
     * instances.
     */
    private static final DatagramSocketFactory DEFAULT_DATAGRAM_SOCKET_FACTORY = new DefaultDatagramSocketFactory();
    /**
     * The timeout to use after opening a socket.
     */
    private int timeout;
    /**
     * The datagram socket used for the connection.
     */
    private DatagramSocket datagramSocket;
    /**
     * A status variable indicating if the client's socket is currently open.
     */
    private boolean isOpen;
    /**
     *  The datagram socket's DatagramSocketFactory.
     */
    private DatagramSocketFactory datagramSocketFactory;
    /**
     * Charset to use for byte IO.
     */
    private Charset charset = Charset.defaultCharset();

    /**
     * Default constructor for DatagramSocketClient.  Initializes
     * _socket_ to null, _timeout_ to 0, and _isOpen_ to false.
     */
    public DatagramSocketClient() {
        datagramSocket = null;
        timeout = 0;
        isOpen = false;
        datagramSocketFactory = DEFAULT_DATAGRAM_SOCKET_FACTORY;
    }

    /**
     * Opens a DatagramSocket on the local host at the first available port.
     * Also sets the timeout on the socket to the default timeout set
     * by {@link #setDefaultTimeout  setDefaultTimeout() }.
     *
     * _isOpen_ is set to true after calling this method and _socket_
     * is set to the newly opened socket.
     *
     * @throws SocketException If the socket could not be opened or the
     *   timeout could not be set.
     */
    public void open() throws SocketException {
        datagramSocket = datagramSocketFactory.createDatagramSocket();
        datagramSocket.setSoTimeout(timeout);
        isOpen = true;
    }

    /**
     * Opens a DatagramSocket on the local host at a specified port.
     * Also sets the timeout on the socket to the default timeout set
     * by {@link #setDefaultTimeout  setDefaultTimeout() }.
     * <p>
     * _isOpen_ is set to true after calling this method and _socket_
     * is set to the newly opened socket.
     *
     * @param port The port to use for the socket.
     * @throws SocketException If the socket could not be opened or the
     *   timeout could not be set.
     ***/
    public void open(int port) throws SocketException {
        datagramSocket = datagramSocketFactory.createDatagramSocket(port);
        datagramSocket.setSoTimeout(timeout);
        isOpen = true;
    }


    /***
     * Opens a DatagramSocket at the specified address on the local host
     * at a specified port.
     * Also sets the timeout on the socket to the default timeout set
     * by {@link #setDefaultTimeout  setDefaultTimeout() }.
     * <p>
     * _isOpen_ is set to true after calling this method and _socket_
     * is set to the newly opened socket.
     *
     * @param port The port to use for the socket.
     * @param laddr  The local address to use.
     * @throws SocketException If the socket could not be opened or the
     *   timeout could not be set.
     ***/
    public void open(int port, InetAddress laddr) throws SocketException {
        datagramSocket = datagramSocketFactory.createDatagramSocket(port, laddr);
        datagramSocket.setSoTimeout(timeout);
        isOpen = true;
    }


    /***
     * Closes the DatagramSocket used for the connection.
     * You should call this method after you've finished using the class
     * instance and also before you call {@link #open open() }
     * again.   _isOpen_ is set to false and  _socket_ is set to null.
     ***/
    public void close() {
        if (datagramSocket != null) {
            datagramSocket.close();
        }
        datagramSocket = null;
        isOpen = false;
    }


    /***
     * Returns true if the client has a currently open socket.
     *
     * @return True if the client has a currently open socket, false otherwise.
     ***/
    public boolean isOpen() {
        return isOpen;
    }

    /***
     * Returns the default timeout in milliseconds that is used when
     * opening a socket.
     *
     * @return The default timeout in milliseconds that is used when
     *         opening a socket.
     ***/
    public int getDefaultTimeout() {
        return timeout;
    }

    /***
     * Set the default timeout in milliseconds to use when opening a socket.
     * After a call to open, the timeout for the socket is set using this value.
     * This method should be used prior to a call to {@link #open open()}
     * and should not be confused with {@link #setSoTimeout setSoTimeout()}
     * which operates on the currently open socket.  _timeout_ contains
     * the new timeout value.
     *
     * @param timeout  The timeout in milliseconds to use for the datagram socket
     *                 connection.
     ***/
    public void setDefaultTimeout(int timeout) {
        this.timeout = timeout;
    }

    /***
     * Returns the timeout in milliseconds of the currently opened socket.
     * If you call this method when the client socket is not open,
     * a NullPointerException is thrown.
     *
     * @return The timeout in milliseconds of the currently opened socket.
     * @throws SocketException if an error getting the timeout
     ***/
    public int getSoTimeout() throws SocketException {
        return datagramSocket.getSoTimeout();
    }

    /***
     * Set the timeout in milliseconds of a currently open connection.
     * Only call this method after a connection has been opened
     * by {@link #open open()}.
     *
     * @param timeout  The timeout in milliseconds to use for the currently
     *                 open datagram socket connection.
     * @throws SocketException if an error setting the timeout
     ***/
    public void setSoTimeout(int timeout) throws SocketException {
        datagramSocket.setSoTimeout(timeout);
    }

    /*
     * Returns the port number of the open socket on the local host used
     * for the connection.  If you call this method when the client socket
     * is not open, a NullPointerException is thrown.
     *
     * @return The port number of the open socket on the local host used
     *         for the connection.
     */
    public int getLocalPort() {
        return datagramSocket.getLocalPort();
    }


    /**
     * Returns the local address to which the client's socket is bound.
     * If you call this method when the client socket is not open, a
     * NullPointerException is thrown.
     *
     * @return The local address to which the client's socket is bound.
     */
    public InetAddress getLocalAddress() {
        return datagramSocket.getLocalAddress();
    }

    /**
     * Sets the DatagramSocketFactory used by the DatagramSocketClient
     * to open DatagramSockets.  If the factory value is null, then a default
     * factory is used (only do this to reset the factory after having
     * previously altered it).
     *
     * @param factory  The new DatagramSocketFactory the DatagramSocketClient
     * should use.
     */
    public void setDatagramSocketFactory(DatagramSocketFactory factory) {
        if (factory == null) {
            datagramSocketFactory = DEFAULT_DATAGRAM_SOCKET_FACTORY;
        } else {
            datagramSocketFactory = factory;
        }
    }

    /**
     * Gets the charset.
     *
     * @return the charset.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Sets the charset.
     *
     * @param charset the charset.
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }
}
