package org.xbib.io.ftp.fs;

import org.xbib.io.ftp.client.FTP;
import org.xbib.io.ftp.client.FTPClient;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.OpenOption;

/**
 * The possible FTP file transfer modes.
 */
public enum FileTransferMode implements OpenOption, CopyOption {
    /**
     * Indicates that files are to be transfered as streams of bytes.
     */
    STREAM(FTP.STREAM_TRANSFER_MODE),
    /**
     * Indicates that files are to be transfered as series of blocks.
     */
    BLOCK(FTP.BLOCK_TRANSFER_MODE),
    /**
     * Indicate that files are to be transfered as FTP compressed data.
     */
    COMPRESSED(FTP.COMPRESSED_TRANSFER_MODE),;

    private final int mode;

    FileTransferMode(int mode) {
        this.mode = mode;
    }

    void apply(FTPClient client) throws IOException {
        if (!client.setFileTransferMode(mode)) {
            throw new FTPFileSystemException(client.getReplyCode(), client.getReplyString());
        }
    }
}
