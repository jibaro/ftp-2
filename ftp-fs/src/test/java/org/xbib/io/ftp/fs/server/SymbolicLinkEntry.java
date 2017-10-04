package org.xbib.io.ftp.fs.server;

import org.mockftpserver.fake.filesystem.FileSystemEntry;
import org.mockftpserver.fake.filesystem.Permissions;

import java.util.Date;

/**
 * A representation of symbolic links.
 */
public class SymbolicLinkEntry implements FileSystemEntry {

    public static final long SIZE = 64;

    private final String path;
    private final FileSystemEntry target;
    private final Permissions permissions;

    public SymbolicLinkEntry(String path, FileSystemEntry target) {
        this.path = path;
        this.target = target;
        this.permissions = new Permissions("rwxrwxrwx");
    }

    @Override
    public boolean isDirectory() {
        return target.isDirectory();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getName() {
        int separatorIndex1 = path.lastIndexOf('/');
        int separatorIndex2 = path.lastIndexOf('\\');
        int separatorIndex = separatorIndex1 > separatorIndex2 ? separatorIndex1 : separatorIndex2;
        return (separatorIndex == -1) ? path : path.substring(separatorIndex + 1);
    }

    public FileSystemEntry getTarget() {
        return target;
    }

    public FileSystemEntry resolve() {
        FileSystemEntry entry = target;
        while (entry instanceof SymbolicLinkEntry) {
            entry = ((SymbolicLinkEntry) entry).target;
        }
        return entry;
    }

    @Override
    public long getSize() {
        return SIZE;
    }

    @Override
    public Date getLastModified() {
        return target.getLastModified();
    }

    @Override
    public void setLastModified(Date lastModified) {
        target.setLastModified(lastModified);
    }

    @Override
    public String getOwner() {
        return target.getOwner();
    }

    @Override
    public String getGroup() {
        return target.getGroup();
    }

    @Override
    public Permissions getPermissions() {
        return permissions;
    }

    @Override
    public FileSystemEntry cloneWithNewPath(String path) {
        return new SymbolicLinkEntry(path, target);
    }

    @Override
    public void lockPath() {
        // path is already read-only
    }

    @Override
    public String toString() {
        return "SymbolicLink['" + getPath() + "' target='" + target + "' size=" + getSize() + " lastModified=" + getLastModified() + " owner="
                + getOwner() + " group=" + getGroup() + " permissions=" + getPermissions() + "]";
    }
}
