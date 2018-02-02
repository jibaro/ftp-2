package org.xbib.io.ftp.fs;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.util.Collection;

/**
 * A default {@link FileSystemExceptionFactory} that always returns an {@link FTPFileSystemException}
 * unless specified otherwise.
 */
public class DefaultFileSystemExceptionFactory implements FileSystemExceptionFactory {

    static final DefaultFileSystemExceptionFactory INSTANCE = new DefaultFileSystemExceptionFactory();

    @Override
    public FileSystemException createGetFileException(String file, int replyCode, String replyString) {
        return new NoSuchFileException(file);
    }

    @Override
    public FileSystemException createChangeWorkingDirectoryException(String directory, int replyCode, String replyString) {
        return new FTPFileSystemException(directory, replyCode, replyString);
    }

    @Override
    public FileAlreadyExistsException createCreateDirectoryException(String directory, int replyCode, String replyString) {
        return new FileAlreadyExistsException(directory);
    }

    @Override
    public FileSystemException createDeleteException(String file, int replyCode, String replyString, boolean isDirectory) {
        return new FTPFileSystemException(file, replyCode, replyString);
    }

    @Override
    public FileSystemException createNewInputStreamException(String file, int replyCode, String replyString) {
        return new FTPFileSystemException(file, replyCode, replyString);
    }

    @Override
    public FileSystemException createNewOutputStreamException(String file, int replyCode, String replyString,
                                                              Collection<? extends OpenOption> options) {
        return new FTPFileSystemException(file, replyCode, replyString);
    }

    @Override
    public FileSystemException createCopyException(String file, String other, int replyCode, String replyString) {
        return new FTPFileSystemException(file, other, replyCode, replyString);
    }

    @Override
    public FileSystemException createMoveException(String file, String other, int replyCode, String replyString) {
        return new FTPFileSystemException(file, other, replyCode, replyString);
    }
}
