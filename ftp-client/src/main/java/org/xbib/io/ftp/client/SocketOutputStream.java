package org.xbib.io.ftp.client;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class wraps an output stream, storing a reference to its originating
 * socket.  When the stream is closed, it will also close the socket
 * immediately afterward.  This class is useful for situations where you
 * are dealing with a stream originating from a socket, but do not have
 * a reference to the socket, and want to make sure it closes when the
 * stream closes.
 *
 * @see SocketInputStream
 */
public class SocketOutputStream extends FilterOutputStream {

    private final Socket socket;

    /**
     * Creates a SocketOutputStream instance wrapping an output stream and
     * storing a reference to a socket that should be closed on closing
     * the stream.
     *
     * @param socket  The socket to close on closing the stream.
     * @param stream  The input stream to wrap.
     */
    public SocketOutputStream(Socket socket, OutputStream stream) {
        super(stream);
        this.socket = socket;
    }

    /**
     * Writes a number of bytes from a byte array to the stream starting from
     * a given offset.  This method bypasses the equivalent method in
     * FilterOutputStream because the FilterOutputStream implementation is
     * very inefficient.
     *
     * @param buffer  The byte array to write.
     * @param offset  The offset into the array at which to start copying data.
     * @param length  The number of bytes to write.
     * @throws IOException If an error occurs while writing to the underlying
     *            stream.
     */
    @Override
    public void write(byte buffer[], int offset, int length) throws IOException {
        out.write(buffer, offset, length);
    }

    /**
     * Closes the stream and immediately afterward closes the referenced
     * socket.
     *
     * @throws IOException  If there is an error in closing the stream
     *                         or socket.
     */
    @Override
    public void close() throws IOException {
        super.close();
        socket.close();
    }
}
