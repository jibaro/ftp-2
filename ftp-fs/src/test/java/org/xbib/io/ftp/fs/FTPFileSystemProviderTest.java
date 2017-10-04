package org.xbib.io.ftp.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class FTPFileSystemProviderTest extends AbstractFTPFileSystemTest {

    public FTPFileSystemProviderTest() {
        // there's no need to test the FTP file system itself, so just use UNIX
        super(true);
    }

    @Test
    public void testPathsAndFilesSupport() throws IOException {

        try (FTPFileSystem fs = (FTPFileSystem) FileSystems.newFileSystem(getURI(), createEnv())) {
            Path path = Paths.get(URI.create(getBaseUrl() + "/foo"));
            assertThat(path, instanceOf(FTPPath.class));
            // as required by Paths.get
            assertEquals(path, path.toAbsolutePath());

            // the file does not exist yet
            assertFalse(Files.exists(path));

            Files.createFile(path);
            try {
                // the file now exists
                assertTrue(Files.exists(path));

                byte[] content = new byte[1024];
                new Random().nextBytes(content);
                try (OutputStream output = Files.newOutputStream(path, FileType.binary())) {
                    output.write(content);
                }

                // check the file directly
                FileEntry file = getFile("/foo");
                assertArrayEquals(content, getContents(file));

            } finally {

                Files.delete(path);
                assertFalse(Files.exists(path));

                assertNull(getFileSystemEntry("/foo"));
            }
        }
    }

    @Test(expected = FileSystemNotFoundException.class)
    public void testPathsAndFilesSupportFileSystemNotFound() {
        Paths.get(URI.create("ftp://ftp.github.com/"));
    }

    // FTPFileSystemProvider.removeFileSystem

    @Test(expected = FileSystemNotFoundException.class)
    public void testRemoveFileSystem() throws IOException {
        addDirectory("/foo/bar");

        FTPFileSystemProvider provider = new FTPFileSystemProvider();
        URI uri;
        try (FTPFileSystem fs = (FTPFileSystem) provider.newFileSystem(getURI(), createEnv())) {
            FTPPath path = new FTPPath(fs, "/foo/bar");

            uri = path.toUri();

            assertFalse(provider.isHidden(path));
        }
        provider.getPath(uri);
    }

    // FTPFileSystemProvider.getPath

    @Test
    public void testGetPath() throws IOException {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("/", "/");
        inputs.put("foo", "/home/test/foo");
        inputs.put("/foo", "/foo");
        inputs.put("foo/bar", "/home/test/foo/bar");
        inputs.put("/foo/bar", "/foo/bar");

        FTPFileSystemProvider provider = new FTPFileSystemProvider();
        try (FTPFileSystem fs = (FTPFileSystem) provider.newFileSystem(getURI(), createEnv())) {
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                URI uri = fs.getPath(entry.getKey()).toUri();
                Path path = provider.getPath(uri);
                assertThat(path, instanceOf(FTPPath.class));
                assertEquals(entry.getValue(), ((FTPPath) path).path());
            }
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                URI uri = fs.getPath(entry.getKey()).toUri();
                uri = URISupport.create(uri.getScheme().toUpperCase(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), null, null);
                Path path = provider.getPath(uri);
                assertThat(path, instanceOf(FTPPath.class));
                assertEquals(entry.getValue(), ((FTPPath) path).path());
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPathNoScheme() {
        FTPFileSystemProvider provider = new FTPFileSystemProvider();
        provider.getPath(URI.create("/foo/bar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPathInvalidScheme() {
        FTPFileSystemProvider provider = new FTPFileSystemProvider();
        provider.getPath(URI.create("https://www.github.com/"));
    }

    @Test(expected = FileSystemNotFoundException.class)
    public void testGetPathFileSystemNotFound() {
        FTPFileSystemProvider provider = new FTPFileSystemProvider();
        provider.getPath(URI.create("ftp://ftp.github.com/"));
    }

    // FTPFileSystemProvider.getFileAttributeView

    @Test
    public void testGetFileAttributeViewBasic() throws IOException {

        FTPFileSystemProvider provider = new FTPFileSystemProvider();
        try (FTPFileSystem fs = (FTPFileSystem) provider.newFileSystem(getURI(), createEnv())) {
            FTPPath path = new FTPPath(fs, "/foo/bar");

            BasicFileAttributeView view = fs.provider().getFileAttributeView(path, BasicFileAttributeView.class);
            assertNotNull(view);
            assertEquals("basic", view.name());
        }
    }

    @Test
    public void testGetFileAttributeViewPosix() throws IOException {

        FTPFileSystemProvider provider = new FTPFileSystemProvider();
        try (FTPFileSystem fs = (FTPFileSystem) provider.newFileSystem(getURI(), createEnv())) {
            FTPPath path = new FTPPath(fs, "/foo/bar");

            PosixFileAttributeView view = fs.provider().getFileAttributeView(path, PosixFileAttributeView.class);
            assertNotNull(view);
            assertEquals("posix", view.name());
        }
    }

    @Test
    public void testGetFileAttributeViewReadAttributes() throws IOException {
        addDirectory("/foo/bar");

        FTPFileSystemProvider provider = new FTPFileSystemProvider();
        try (FTPFileSystem fs = (FTPFileSystem) provider.newFileSystem(getURI(), createEnv())) {
            FTPPath path = new FTPPath(fs, "/foo/bar");

            BasicFileAttributeView view = fs.provider().getFileAttributeView(path, BasicFileAttributeView.class);
            assertNotNull(view);

            BasicFileAttributes attributes = view.readAttributes();
            assertTrue(attributes.isDirectory());
        }
    }
}
