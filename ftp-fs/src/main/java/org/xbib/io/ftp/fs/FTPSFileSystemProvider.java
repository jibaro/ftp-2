package org.xbib.io.ftp.fs;

import java.util.Map;

/**
 * A provider for FTPS file systems.
 */
public class FTPSFileSystemProvider extends FTPFileSystemProvider {

    /**
     * Returns the URI scheme that identifies this provider: {@code ftps}.
     */
    @Override
    public String getScheme() {
        return "ftps";
    }

    @Override
    FTPSEnvironment wrapEnvironment(Map<String, ?> env) {
        return FTPSEnvironment.wrap(env);
    }
}
