package org.xbib.io.ftp.client;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

/***
 * DefaultSocketFactory implements the SocketFactory interface by
 * simply wrapping the java.net.Socket and java.net.ServerSocket
 * constructors.  It is the default SocketFactory used by
 * {@link SocketClient}
 * implementations.
 *
 * @see SocketFactory
 * @see SocketClient
 * @see SocketClient#setSocketFactory
 ***/

public class DefaultSocketFactory extends SocketFactory {
    /**
     * The proxy to use when creating new sockets.
     */
    private final Proxy connProxy;

    /**
     * The default constructor.
     */
    public DefaultSocketFactory() {
        this(null);
    }

    /**
     * A constructor for sockets with proxy support.
     *
     * @param proxy The Proxy to use when creating new Sockets.
     */
    public DefaultSocketFactory(Proxy proxy) {
        connProxy = proxy;
    }

    /**
     * Creates an unconnected Socket.
     *
     * @return A new unconnected Socket.
     * @throws IOException If an I/O error occurs while creating the Socket.
     */
    @Override
    public Socket createSocket() throws IOException {
        if (connProxy != null) {
            return new Socket(connProxy);
        }
        return new Socket();
    }

    /***
     * Creates a Socket connected to the given host and port.
     *
     * @param host The hostname to connect to.
     * @param port The port to connect to.
     * @return A Socket connected to the given host and port.
     * @throws UnknownHostException  If the hostname cannot be resolved.
     * @throws IOException If an I/O error occurs while creating the Socket.
     ***/
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        if (connProxy != null) {
            Socket s = new Socket(connProxy);
            s.connect(new InetSocketAddress(host, port));
            return s;
        }
        return new Socket(host, port);
    }

    /***
     * Creates a Socket connected to the given host and port.
     *
     * @param address The address of the host to connect to.
     * @param port The port to connect to.
     * @return A Socket connected to the given host and port.
     * @throws IOException If an I/O error occurs while creating the Socket.
     ***/
    @Override
    public Socket createSocket(InetAddress address, int port) throws IOException {
        if (connProxy != null) {
            Socket s = new Socket(connProxy);
            s.connect(new InetSocketAddress(address, port));
            return s;
        }
        return new Socket(address, port);
    }

    /***
     * Creates a Socket connected to the given host and port and
     * originating from the specified local address and port.
     *
     * @param host The hostname to connect to.
     * @param port The port to connect to.
     * @param localAddr  The local address to use.
     * @param localPort  The local port to use.
     * @return A Socket connected to the given host and port.
     * @throws UnknownHostException  If the hostname cannot be resolved.
     * @throws IOException If an I/O error occurs while creating the Socket.
     ***/
    @Override
    public Socket createSocket(String host, int port,
                               InetAddress localAddr, int localPort) throws IOException {
        if (connProxy != null) {
            Socket s = new Socket(connProxy);
            s.bind(new InetSocketAddress(localAddr, localPort));
            s.connect(new InetSocketAddress(host, port));
            return s;
        }
        return new Socket(host, port, localAddr, localPort);
    }

    /***
     * Creates a Socket connected to the given host and port and
     * originating from the specified local address and port.
     *
     * @param address The address of the host to connect to.
     * @param port The port to connect to.
     * @param localAddr  The local address to use.
     * @param localPort  The local port to use.
     * @return A Socket connected to the given host and port.
     * @throws IOException If an I/O error occurs while creating the Socket.
     ***/
    @Override
    public Socket createSocket(InetAddress address, int port,
                               InetAddress localAddr, int localPort) throws IOException {
        if (connProxy != null) {
            Socket s = new Socket(connProxy);
            s.bind(new InetSocketAddress(localAddr, localPort));
            s.connect(new InetSocketAddress(address, port));
            return s;
        }
        return new Socket(address, port, localAddr, localPort);
    }
}
