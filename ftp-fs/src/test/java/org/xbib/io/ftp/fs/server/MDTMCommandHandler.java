package org.xbib.io.ftp.fs.server;

import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.ReplyCodes;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.fake.command.AbstractFakeCommandHandler;
import org.mockftpserver.fake.filesystem.FileSystemEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A command handler for the MDTM command.
 */
public class MDTMCommandHandler extends AbstractFakeCommandHandler {

    @Override
    protected void handle(Command command, Session session) {
        verifyLoggedIn(session);

        String path = getRealPath(session, command.getParameter(0));

        verifyFileSystemCondition(getFileSystem().exists(path), path, "filesystem.doesNotExist");
        verifyReadPermission(session, path);

        FileSystemEntry entry = getFileSystem().getEntry(path);
        session.sendReply(ReplyCodes.STAT_FILE_OK, getResponse(entry.getLastModified()));
    }

    private String getResponse(Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }
}
