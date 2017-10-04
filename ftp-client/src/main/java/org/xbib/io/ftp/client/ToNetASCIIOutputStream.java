package org.xbib.io.ftp.client;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class wraps an output stream, replacing all singly occurring
 * &lt;LF&gt; (linefeed) characters with &lt;CR&gt;&lt;LF&gt; (carriage return
 * followed by linefeed), which is the NETASCII standard for representing
 * a newline.
 * You would use this class to implement ASCII file transfers requiring
 * conversion to NETASCII.
 */
public final class ToNetASCIIOutputStream extends FilterOutputStream {
    private boolean lastWasCR;

    /**
     * Creates a ToNetASCIIOutputStream instance that wraps an existing
     * OutputStream.
     *
     * @param output  The OutputStream to wrap.
     */
    public ToNetASCIIOutputStream(OutputStream output) {
        super(output);
        lastWasCR = false;
    }

    /**
     * Writes a byte to the stream.    Note that a call to this method
     * may result in multiple writes to the underlying input stream in order
     * to convert naked newlines to NETASCII line separators.
     * This is transparent to the programmer and is only mentioned for
     * completeness.
     *
     * @param ch The byte to write.
     * @throws IOException If an error occurs while writing to the underlying
     *            stream.
     */
    @Override
    public synchronized void write(int ch) throws IOException {
        switch (ch) {
            case '\r':
                lastWasCR = true;
                out.write('\r');
                return;
            case '\n':
                if (!lastWasCR) {
                    out.write('\r');
                }
                lastWasCR = false;
                out.write(ch);
                return;
            default:
                lastWasCR = false;
                out.write(ch);
        }
    }

    /**
     * Writes a byte array to the stream.
     *
     * @param buffer  The byte array to write.
     * @throws IOException If an error occurs while writing to the underlying
     *            stream.
     */
    @Override
    public synchronized void write(byte buffer[]) throws IOException {
        write(buffer, 0, buffer.length);
    }

    /**
     * Writes a number of bytes from a byte array to the stream starting from
     * a given offset.
     *
     * @param buffer  The byte array to write.
     * @param offset  The offset into the array at which to start copying data.
     * @param length  The number of bytes to write.
     * @throws IOException If an error occurs while writing to the underlying
     *            stream.
     */
    @Override
    public synchronized void write(byte buffer[], int offset, int length) throws IOException {
        while (length-- > 0) {
            write(buffer[offset++]);
        }
    }

}
