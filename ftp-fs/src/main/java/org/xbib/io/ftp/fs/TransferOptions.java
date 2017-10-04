package org.xbib.io.ftp.fs;

/**
 * The base class of option combinations that support file transfers.
 */
abstract class TransferOptions {

    public final FileType fileType;
    public final FileStructure fileStructure;
    public final FileTransferMode fileTransferMode;

    TransferOptions(FileType fileType, FileStructure fileStructure, FileTransferMode fileTransferMode) {
        this.fileType = fileType;
        this.fileStructure = fileStructure;
        this.fileTransferMode = fileTransferMode;
    }
}
