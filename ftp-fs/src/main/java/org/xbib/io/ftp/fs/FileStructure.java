package org.xbib.io.ftp.fs;

import org.xbib.io.ftp.client.FTP;
import org.xbib.io.ftp.client.FTPClient;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.OpenOption;

/**
 * The possible FTP file structures.
 */
public enum FileStructure implements OpenOption, CopyOption {
    /**
     * Indicates that files are to be treated as a continuous sequence of bytes.
     */
    FILE(FTP.FILE_STRUCTURE),
    /**
     * Indicates that files are to be treated as a sequence of records.
     */
    RECORD(FTP.RECORD_STRUCTURE),
    /**
     * Indicates that files are to be treated as a set of independent indexed pages.
     */
    PAGE(FTP.PAGE_STRUCTURE),;

    private final int structure;

    FileStructure(int structure) {
        this.structure = structure;
    }

    void apply(FTPClient client) throws IOException {
        if (!client.setFileStructure(structure)) {
            throw new FTPFileSystemException(client.getReplyCode(), client.getReplyString());
        }
    }
}
