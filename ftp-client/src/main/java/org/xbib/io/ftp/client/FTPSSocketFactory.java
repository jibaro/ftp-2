package org.xbib.io.ftp.client;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Implementation of {@link SocketFactory}.
 *
 */
public class FTPSSocketFactory extends SocketFactory {

    private final SSLContext context;

    public FTPSSocketFactory(SSLContext context) {
        this.context = context;
    }

    // Override the default implementation
    @Override
    public Socket createSocket() throws IOException {
        return this.context.getSocketFactory().createSocket();
    }

    @Override
    public Socket createSocket(String address, int port) throws IOException {
        return this.context.getSocketFactory().createSocket(address, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port) throws IOException {
        return this.context.getSocketFactory().createSocket(address, port);
    }

    @Override
    public Socket createSocket(String address, int port, InetAddress localAddress, int localPort)
            throws IOException {
        return this.context.getSocketFactory().createSocket(address, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return this.context.getSocketFactory().createSocket(address, port, localAddress, localPort);
    }
}
