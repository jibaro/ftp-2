package org.xbib.groovy.ftp;

import org.xbib.io.ftp.fs.FTPEnvironment;
import org.xbib.io.ftp.fs.FTPFileSystemProvider;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.util.Map;

/**
 */
class FTPContext {

    final FileSystem fileSystem;

    FTPContext(URI uri, Map<String, ?> env) throws IOException {
        this.fileSystem = env != null ?
                new FTPFileSystemProvider().newFileSystem(uri, env) :
                new FTPFileSystemProvider().newFileSystem(uri, new FTPEnvironment());
    }

    void close() throws IOException {
        fileSystem.close();
    }
}
