package org.xbib.io.ftp.fs;

import org.xbib.io.ftp.client.FTPFile;
import org.xbib.io.ftp.client.FTPFileFilter;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.List;

/**
 * A strategy for handling FTP files in an FTP server specific way.
 * This will help support FTP servers that return the current directory (.) when
 * listing directories, and FTP servers that don't.
 */
abstract class FTPFileStrategy {

    static FTPFileStrategy getInstance(FTPClientPool.Client client) throws IOException {
        FTPFile[] ftpFiles = client.listFiles("/", new FTPFileFilter() {
            @Override
            public boolean accept(FTPFile ftpFile) {
                String fileName = FTPFileSystem.getFileName(ftpFile);
                return FTPFileSystem.CURRENT_DIR.equals(fileName);
            }
        });
        return ftpFiles.length == 0 ? NonUnix.INSTANCE : Unix.INSTANCE;
    }

    abstract List<FTPFile> getChildren(FTPClientPool.Client client, FTPPath path) throws IOException;

    abstract FTPFile getFTPFile(FTPClientPool.Client client, FTPPath path) throws IOException;

    abstract FTPFile getLink(FTPClientPool.Client client, FTPFile ftpFile, FTPPath path) throws IOException;

    private static final class Unix extends FTPFileStrategy {

        private static final FTPFileStrategy INSTANCE = new Unix();

        @Override
        List<FTPFile> getChildren(FTPClientPool.Client client, FTPPath path) throws IOException {

            FTPFile[] ftpFiles = client.listFiles(path.path());

            if (ftpFiles.length == 0) {
                throw new NoSuchFileException(path.path());
            }
            boolean isDirectory = false;
            List<FTPFile> children = new ArrayList<>(ftpFiles.length);
            for (FTPFile ftpFile : ftpFiles) {
                String fileName = FTPFileSystem.getFileName(ftpFile);
                if (FTPFileSystem.CURRENT_DIR.equals(fileName)) {
                    isDirectory = true;
                } else if (!FTPFileSystem.PARENT_DIR.equals(fileName)) {
                    children.add(ftpFile);
                }
            }

            if (!isDirectory) {
                throw new NotDirectoryException(path.path());
            }

            return children;
        }

        @Override
        FTPFile getFTPFile(FTPClientPool.Client client, FTPPath path) throws IOException {
            final String name = path.fileName();

            FTPFile[] ftpFiles = client.listFiles(path.path(), new FTPFileFilter() {
                @Override
                public boolean accept(FTPFile ftpFile) {
                    String fileName = FTPFileSystem.getFileName(ftpFile);
                    return FTPFileSystem.CURRENT_DIR.equals(fileName) || (name != null && name.equals(fileName));
                }
            });
            client.throwIfEmpty(path.path(), ftpFiles);
            if (ftpFiles.length == 1) {
                return ftpFiles[0];
            }
            for (FTPFile ftpFile : ftpFiles) {
                if (FTPFileSystem.CURRENT_DIR.equals(FTPFileSystem.getFileName(ftpFile))) {
                    return ftpFile;
                }
            }
            throw new IllegalStateException();
        }

        @Override
        FTPFile getLink(FTPClientPool.Client client, FTPFile ftpFile, FTPPath path) throws IOException {
            if (ftpFile.getLink() != null) {
                return ftpFile;
            }
            if (ftpFile.isDirectory() && FTPFileSystem.CURRENT_DIR.equals(FTPFileSystem.getFileName(ftpFile))) {
                // The file is returned using getFTPFile, which returns the . (current directory) entry for directories.
                // List the parent (if any) instead.

                final String parentPath = path.toAbsolutePath().parentPath();
                final String name = path.fileName();

                if (parentPath == null) {
                    // path is /, there is no link
                    return null;
                }

                FTPFile[] ftpFiles = client.listFiles(parentPath, new FTPFileFilter() {
                    @Override
                    public boolean accept(FTPFile ftpFile) {
                        return (ftpFile.isDirectory() || ftpFile.isSymbolicLink()) && name.equals(FTPFileSystem.getFileName(ftpFile));
                    }
                });
                client.throwIfEmpty(path.path(), ftpFiles);
                return ftpFiles[0].getLink() == null ? null : ftpFiles[0];
            }
            return null;
        }
    }

    private static final class NonUnix extends FTPFileStrategy {

        private static final FTPFileStrategy INSTANCE = new NonUnix();

        @Override
        List<FTPFile> getChildren(FTPClientPool.Client client, FTPPath path) throws IOException {

            FTPFile[] ftpFiles = client.listFiles(path.path());

            boolean isDirectory = false;
            List<FTPFile> children = new ArrayList<>(ftpFiles.length);
            for (FTPFile ftpFile : ftpFiles) {
                String fileName = FTPFileSystem.getFileName(ftpFile);
                if (FTPFileSystem.CURRENT_DIR.equals(fileName)) {
                    isDirectory = true;
                } else if (!FTPFileSystem.PARENT_DIR.equals(fileName)) {
                    children.add(ftpFile);
                }
            }

            if (!isDirectory && children.size() <= 1) {
                // either zero or one, check the parent to see if the path exists and is a directory
                FTPPath currentPath = path;
                FTPFile currentFtpFile = getFTPFile(client, currentPath);
                while (currentFtpFile.isSymbolicLink()) {
                    currentPath = path.resolve(currentFtpFile.getLink());
                    currentFtpFile = getFTPFile(client, currentPath);
                }
                if (!currentFtpFile.isDirectory()) {
                    throw new NotDirectoryException(path.path());
                }
            }

            return children;
        }

        @Override
        FTPFile getFTPFile(FTPClientPool.Client client, FTPPath path) throws IOException {
            final String parentPath = path.toAbsolutePath().parentPath();
            final String name = path.fileName();
            if (parentPath == null) {
                // path is /, but that cannot be listed
                FTPFile rootFtpFile = new FTPFile();
                rootFtpFile.setName("/");
                rootFtpFile.setType(FTPFile.DIRECTORY_TYPE);
                return rootFtpFile;
            }
            FTPFile[] ftpFiles = client.listFiles(parentPath,
                    ftpFile -> name != null && name.equals(FTPFileSystem.getFileName(ftpFile)));
            if (ftpFiles.length == 0) {
                throw new NoSuchFileException(path.path());
            }
            if (ftpFiles.length == 1) {
                return ftpFiles[0];
            }
            throw new IllegalStateException();
        }

        @Override
        FTPFile getLink(FTPClientPool.Client client, FTPFile ftpFile, FTPPath path) throws IOException {
            // getFTPFile always returns the entry in the parent, so there's no need to list the parent here.
            return ftpFile.getLink() == null ? null : ftpFile;
        }
    }
}
