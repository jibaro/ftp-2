package org.xbib.io.ftp.fs.server;

import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.ReplyCodes;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.core.util.StringUtil;
import org.mockftpserver.fake.command.ListCommandHandler;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileSystemEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A command handler for LIST that supports the {@code -a} flag.
 */
public class ListHiddenFilesCommandHandler extends ListCommandHandler {

    private final boolean includeDotEntry;

    /**
     * Creates a new LIST command handler.
     *
     * @param includeDotEntry {@code true} to include a dot entry, or {@code false} otherwise.
     */
    public ListHiddenFilesCommandHandler(boolean includeDotEntry) {
        this.includeDotEntry = includeDotEntry;
    }

    @Override
    protected void handle(Command command, Session session) {
        if (command.getParameter(0).startsWith("-a ")) {
            String path = command.getParameter(0).substring(3);
            handle(path, session);
        } else {
            super.handle(command, session);
        }
    }

    private void handle(String path, Session session) {
        // code mostly copied from ListCommandHandler.handle, but with added . entry

        verifyLoggedIn(session);

        path = getRealPath(session, path);

        // User must have read permission to the path
        if (getFileSystem().exists(path)) {
            this.replyCodeForFileSystemException = ReplyCodes.READ_FILE_ERROR;
            verifyReadPermission(session, path);
        }

        this.replyCodeForFileSystemException = ReplyCodes.SYSTEM_ERROR;
        List<?> fileEntries = getFileSystem().listFiles(path);
        Iterator<?> iter = fileEntries.iterator();
        List<String> lines = new ArrayList<>();
        while (iter.hasNext()) {
            FileSystemEntry entry = (FileSystemEntry) iter.next();
            lines.add(getFileSystem().formatDirectoryListing(entry));
        }
        FileSystemEntry entry = getFileSystem().getEntry(path);
        if (entry != null && entry.isDirectory() && includeDotEntry) {
            lines.add(0, getFileSystem().formatDirectoryListing(addDot(getFileSystem().getEntry(path))));
        }
        String result = StringUtil.join(lines, endOfLine());
        result += result.length() > 0 ? endOfLine() : "";

        sendReply(session, ReplyCodes.TRANSFER_DATA_INITIAL_OK);

        session.openDataConnection();
        LOG.info("Sending [" + result + "]");
        session.sendData(result.getBytes(), result.length());
        session.closeDataConnection();

        sendReply(session, ReplyCodes.TRANSFER_DATA_FINAL_OK);
    }

    private FileSystemEntry addDot(FileSystemEntry entry) {
        if (entry instanceof SymbolicLinkEntry) {
            entry = ((SymbolicLinkEntry) entry).resolve();
        }
        if (entry instanceof DirectoryEntry) {
            DirectoryEntry newEntry = new DirectoryEntry(entry.getPath() + "/.");
            newEntry.setLastModified(entry.getLastModified());
            newEntry.setOwner(entry.getOwner());
            newEntry.setGroup(entry.getGroup());
            newEntry.setPermissions(entry.getPermissions());
            return newEntry;
        }
        return entry;
    }
}
