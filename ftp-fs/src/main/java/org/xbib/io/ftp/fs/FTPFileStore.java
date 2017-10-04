package org.xbib.io.ftp.fs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.Objects;

/**
 * An FTP file system store.
 */
class FTPFileStore extends FileStore {

    private final FTPFileSystem fs;

    FTPFileStore(FTPFileSystem fs) {
        this.fs = Objects.requireNonNull(fs);
    }

    @Override
    public String name() {
        return fs.toUri("/").toString();
    }

    @Override
    public String type() {
        return fs.isSecure() ? "ftps" : "ftp";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public long getTotalSpace() throws IOException {
        return fs.getTotalSpace();
    }

    @Override
    public long getUsableSpace() throws IOException {
        return fs.getUsableSpace();
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        return fs.getUnallocatedSpace();
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        return type == BasicFileAttributeView.class || type == PosixFileAttributeView.class;
    }

    @Override
    public boolean supportsFileAttributeView(String name) {
        return "basic".equals(name) || "owner".equals(name) || "posix".equals(name);
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        Objects.requireNonNull(type);
        return null;
    }

    @Override
    public Object getAttribute(String attribute) throws IOException {
        if ("totalSpace".equals(attribute)) {
            return getTotalSpace();
        }
        if ("usableSpace".equals(attribute)) {
            return getUsableSpace();
        }
        if ("unallocatedSpace".equals(attribute)) {
            return getUnallocatedSpace();
        }
        throw Messages.fileStore().unsupportedAttribute(attribute);
    }
}
