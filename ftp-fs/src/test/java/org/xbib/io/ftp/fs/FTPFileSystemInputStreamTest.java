package org.xbib.io.ftp.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockftpserver.fake.filesystem.FileEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FTPFileSystemInputStreamTest extends AbstractFTPFileSystemTest {

    public FTPFileSystemInputStreamTest(boolean useUnixFtpServer) {
        super(useUnixFtpServer);
    }

    @Parameters(name = "Use UNIX FTP server: {0}")
    public static List<Object[]> getParameters() {
        Object[][] parameters = {
                {true,},
                {false,},
        };
        return Arrays.asList(parameters);
    }

    @Test
    public void testReadSingle() throws IOException {
        final String content = "Hello World";

        FileEntry file = addFile("/foo");
        file.setContents(content);

        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals('H', input.read());
            assertEquals('e', input.read());
            assertEquals('l', input.read());
            assertEquals('l', input.read());
            assertEquals('o', input.read());
            assertEquals(' ', input.read());
            assertEquals('W', input.read());
            assertEquals('o', input.read());
            assertEquals('r', input.read());
            assertEquals('l', input.read());
            assertEquals('d', input.read());
            assertEquals(-1, input.read());
        }
    }

    @Test
    public void testReadBulk() throws IOException {
        final String content = "Hello World";

        FileEntry file = addFile("/foo");
        file.setContents(content);

        byte[] b = new byte[20];
        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals(0, input.read(b, 0, 0));
            assertEquals(5, input.read(b, 1, 5));
            assertArrayEquals(content.substring(0, 5).getBytes(), Arrays.copyOfRange(b, 1, 6));
            assertEquals(content.length() - 5, input.read(b));
            assertArrayEquals(content.substring(5).getBytes(), Arrays.copyOfRange(b, 0, content.length() - 5));
            assertEquals(-1, input.read(b));
        }
    }

    @Test
    public void testSkip() throws IOException {
        final String content = "Hello World";

        FileEntry file = addFile("/foo");
        file.setContents(content);

        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals(0, input.skip(0));
            assertArrayEquals(content.getBytes(), readRemaining(input));
        }
        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals(5, input.skip(5));
            assertArrayEquals(content.substring(5).getBytes(), readRemaining(input));
        }
        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals(content.length(), input.skip(content.length()));
            assertEquals(-1, input.read());
            assertEquals(0, input.skip(1));
        }
        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals(content.length(), input.skip(content.length() + 1));
            assertEquals(-1, input.read());
            assertEquals(0, input.skip(1));
        }
    }

    @Test
    public void testAvailable() throws IOException {
        final String content = "Hello World";
        FileEntry file = addFile("/foo");
        file.setContents(content);
        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            for (int i = 0; i < 5; i++) {
                input.read();
            }
            assertEquals(content.length() - 5, input.available());
            while (input.read() != -1) {
                // do nothing
            }
            assertEquals(0, input.available());
            input.read();
            assertEquals(0, input.available());
        }
    }

    private byte[] readRemaining(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = input.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        }
        return output.toByteArray();
    }
}
