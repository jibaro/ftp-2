package org.xbib.io.ftp.fs.server;

import org.mockftpserver.fake.filesystem.FileSystemEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.util.List;

/**
 * An extended version of {@link UnixFakeFileSystem} that supports symbolic links.
 */
public class ExtendedUnixFakeFileSystem extends UnixFakeFileSystem {

    public ExtendedUnixFakeFileSystem() {
        setDirectoryListingFormatter(new ExtendedUnixDirectoryListingFormatter());
    }

    private String resolveLinks(String path) {
        FileSystemEntry entry = getEntry(path);
        if (entry instanceof SymbolicLinkEntry && entry.isDirectory()) {
            return ((SymbolicLinkEntry) entry).resolve().getPath();
        }
        return path;
    }

    @Override
    public List<?> listFiles(String path) {
        return super.listFiles(resolveLinks(path));
    }

    @Override
    public List<?> listNames(String path) {
        return super.listNames(resolveLinks(path));
    }
}
