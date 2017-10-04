package org.xbib.io.ftp.fs.server;

import org.mockftpserver.fake.filesystem.FileSystemEntry;
import org.mockftpserver.fake.filesystem.UnixDirectoryListingFormatter;

/**
 * An extended version of {@link UnixDirectoryListingFormatter} that supports symbolic links.
 */
public class ExtendedUnixDirectoryListingFormatter extends UnixDirectoryListingFormatter {

    @Override
    public String format(FileSystemEntry fileSystemEntry) {
        String formatted = super.format(fileSystemEntry);
        if (fileSystemEntry instanceof SymbolicLinkEntry) {
            SymbolicLinkEntry symLink = (SymbolicLinkEntry) fileSystemEntry;
            formatted = "l" + formatted.substring(1) + " -> " + symLink.getTarget().getPath();
        }
        return formatted;
    }
}
