package org.xbib.io.ftp.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockftpserver.fake.filesystem.FileEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FTPFileSystemOutputStreamTest extends AbstractFTPFileSystemTest {

    public FTPFileSystemOutputStreamTest(boolean useUnixFtpServer) {
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
    public void testWriteSingle() throws IOException {

        try (OutputStream output = getFileSystem().newOutputStream(createPath("/foo"))) {
            output.write('H');
            output.write('e');
            output.write('l');
            output.write('l');
            output.write('o');
        }
        FileEntry file = getFile("/foo");
        assertEquals("Hello", getStringContents(file));
    }

    @Test
    public void testWriteBulk() throws IOException {

        try (OutputStream output = getFileSystem().newOutputStream(createPath("/foo"))) {
            output.write("Hello".getBytes());
        }
        FileEntry file = getFile("/foo");
        assertEquals("Hello", getStringContents(file));
    }
}
