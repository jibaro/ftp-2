package org.xbib.io.ftp.fs;

import org.xbib.io.ftp.client.FTPClient;

/**
 * The possible FTP connection modes. Note that server-to-server is not supported.
 */
public enum ConnectionMode {
    /**
     * Indicates that FTP servers should connect to clients' data ports to initiate data transfers.
     */
    ACTIVE {
        @Override
        void apply(FTPClient client) {
            client.enterLocalActiveMode();
        }
    },
    /**
     * Indicates that FTP servers are in passive mode, requiring clients to connect to the servers' data ports to initiate transfers.
     */
    PASSIVE {
        @Override
        void apply(FTPClient client) {
            client.enterLocalPassiveMode();
        }
    },;

    abstract void apply(FTPClient client);
}
