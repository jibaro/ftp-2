package org.xbib.io.ftp.fs;

import org.xbib.io.ftp.client.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotLinkException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * An FTP file system.
 */
class FTPFileSystem extends FileSystem {

    static final String CURRENT_DIR = ".";
    static final String PARENT_DIR = "..";

    private static final Set<String> SUPPORTED_FILE_ATTRIBUTE_VIEWS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList("basic", "owner", "posix")));
    private static final Set<String> BASIC_ATTRIBUTES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "basic:lastModifiedTime", "basic:lastAccessTime", "basic:creationTime", "basic:size",
            "basic:isRegularFile", "basic:isDirectory", "basic:isSymbolicLink", "basic:isOther", "basic:fileKey")));
    private static final Set<String> OWNER_ATTRIBUTES = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(
            "owner:owner")));
    private static final Set<String> POSIX_ATTRIBUTES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "posix:lastModifiedTime", "posix:lastAccessTime", "posix:creationTime", "posix:size",
            "posix:isRegularFile", "posix:isDirectory", "posix:isSymbolicLink", "posix:isOther", "posix:fileKey",
            "posix:owner", "posix:group", "posix:permissions")));
    private final FTPFileSystemProvider provider;
    private final Iterable<Path> rootDirectories;
    private final FileStore fileStore;
    private final Iterable<FileStore> fileStores;
    private final FTPClientPool clientPool;
    private final URI uri;
    private final String defaultDirectory;
    private final FTPFileStrategy ftpFileStrategy;
    private final AtomicBoolean open = new AtomicBoolean(true);

    FTPFileSystem(FTPFileSystemProvider provider, URI uri, FTPEnvironment env) throws IOException {
        this.provider = Objects.requireNonNull(provider);
        this.rootDirectories = Collections.<Path>singleton(new FTPPath(this, "/"));
        this.fileStore = new FTPFileStore(this);
        this.fileStores = Collections.<FileStore>singleton(fileStore);

        this.clientPool = new FTPClientPool(uri.getHost(), uri.getPort(), env);
        this.uri = Objects.requireNonNull(uri);

        try (FTPClientPool.Client client = clientPool.get()) {
            this.defaultDirectory = client.pwd();

            this.ftpFileStrategy = FTPFileStrategy.getInstance(client);
        }
    }

    static String getFileName(FTPFile ftpFile) {
        String fileName = ftpFile.getName();
        if (fileName == null) {
            return null;
        }
        int index = fileName.lastIndexOf('/');
        return index == -1 || index == fileName.length() - 1 ? fileName : fileName.substring(index + 1);
    }

    @Override
    public FTPFileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        if (open.getAndSet(false)) {
            provider.removeFileSystem(uri);
            clientPool.close();
        }
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return rootDirectories;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return fileStores;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return SUPPORTED_FILE_ATTRIBUTE_VIEWS;
    }

    @Override
    public Path getPath(String first, String... more) {
        StringBuilder sb = new StringBuilder(first);
        for (String s : more) {
            sb.append("/").append(s);
        }
        return new FTPPath(this, sb.toString());
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        final Pattern pattern = PathMatcherSupport.toPattern(syntaxAndPattern);
        return new PathMatcher() {
            @Override
            public boolean matches(Path path) {
                return pattern.matcher(path.toString()).matches();
            }
        };
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw Messages.unsupportedOperation(FileSystem.class, "getUserPrincipalLookupService");
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw Messages.unsupportedOperation(FileSystem.class, "newWatchService");
    }

    void keepAlive() throws IOException {
        clientPool.keepAlive();
    }

    boolean isSecure() {
        return clientPool.isSecure();
    }

    URI toUri(FTPPath path) {
        FTPPath absPath = toAbsolutePath(path).normalize();
        return toUri(absPath.path());
    }

    URI toUri(String path) {
        return URISupport.create(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, null, null);
    }

    FTPPath toAbsolutePath(FTPPath path) {
        if (path.isAbsolute()) {
            return path;
        }
        return new FTPPath(this, defaultDirectory + "/" + path.path());
    }

    FTPPath toRealPath(FTPPath path, LinkOption... options) throws IOException {
        boolean followLinks = LinkOptionSupport.followLinks(options);
        try (FTPClientPool.Client client = clientPool.get()) {
            return toRealPath(client, path, followLinks).ftpPath;
        }
    }

    private FTPPathAndFilePair toRealPath(FTPClientPool.Client client, FTPPath path, boolean followLinks) throws IOException {
        FTPPath absPath = toAbsolutePath(path).normalize();
        // call getFTPFile to verify the file exists
        FTPFile ftpFile = getFTPFile(client, absPath);

        if (followLinks && isPossibleSymbolicLink(ftpFile)) {
            FTPFile link = getLink(client, ftpFile, absPath);
            if (link != null) {
                return toRealPath(client, new FTPPath(this, link.getLink()), followLinks);
            }
        }
        return new FTPPathAndFilePair(absPath, ftpFile);
    }

    private boolean isPossibleSymbolicLink(FTPFile ftpFile) {
        return ftpFile.isSymbolicLink() || (ftpFile.isDirectory() && CURRENT_DIR.equals(getFileName(ftpFile)));
    }

    String toString(FTPPath path) {
        return path.path();
    }

    InputStream newInputStream(FTPPath path, OpenOption... options) throws IOException {
        OpenOptions openOptions = OpenOptions.forNewInputStream(options);

        try (FTPClientPool.Client client = clientPool.get()) {
            return newInputStream(client, path, openOptions);
        }
    }

    private InputStream newInputStream(FTPClientPool.Client client, FTPPath path, OpenOptions options) throws IOException {
        assert options.read;

        return client.newInputStream(path.path(), options);
    }

    OutputStream newOutputStream(FTPPath path, OpenOption... options) throws IOException {
        OpenOptions openOptions = OpenOptions.forNewOutputStream(options);

        try (FTPClientPool.Client client = clientPool.get()) {
            return newOutputStream(client, path, false, openOptions).out;
        }
    }

    private FTPFileAndOutputStreamPair newOutputStream(FTPClientPool.Client client, FTPPath path, boolean requireFTPFile, OpenOptions options) throws IOException {

        // retrieve the file unless create is true and createNew is false, because then the file can be created
        FTPFile ftpFile = null;
        if (!options.create || options.createNew) {
            ftpFile = findFTPFile(client, path);
            if (ftpFile != null && ftpFile.isDirectory()) {
                throw Messages.fileSystemProvider().isDirectory(path.path());
            }
            if (!options.createNew && ftpFile == null) {
                throw new NoSuchFileException(path.path());
            } else if (options.createNew && ftpFile != null) {
                throw new FileAlreadyExistsException(path.path());
            }
        }
        // else the file can be created if necessary

        if (ftpFile == null && requireFTPFile) {
            ftpFile = findFTPFile(client, path);
        }

        OutputStream out = client.newOutputStream(path.path(), options);
        return new FTPFileAndOutputStreamPair(ftpFile, out);
    }

    SeekableByteChannel newByteChannel(FTPPath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        if (attrs.length > 0) {
            throw Messages.fileSystemProvider().unsupportedCreateFileAttribute(attrs[0].name());
        }

        OpenOptions openOptions = OpenOptions.forNewByteChannel(options);

        try (FTPClientPool.Client client = clientPool.get()) {
            if (openOptions.read) {
                // use findFTPFile instead of getFTPFile, to let the opening of the stream provide the correct error message
                FTPFile ftpFile = findFTPFile(client, path);
                InputStream in = newInputStream(client, path, openOptions);
                long size = ftpFile == null ? 0 : ftpFile.getSize();
                return FileSystemProviderSupport.createSeekableByteChannel(in, size);
            }

            // if append then we need the FTP file, to find the initial position of the channel
            boolean requireFTPFile = openOptions.append;
            FTPFileAndOutputStreamPair outPair = newOutputStream(client, path, requireFTPFile, openOptions);
            long initialPosition = outPair.ftpFile == null ? 0 : outPair.ftpFile.getSize();
            return FileSystemProviderSupport.createSeekableByteChannel(outPair.out, initialPosition);
        }
    }

    DirectoryStream<Path> newDirectoryStream(final FTPPath path, Filter<? super Path> filter) throws IOException {
        List<FTPFile> children;
        try (FTPClientPool.Client client = clientPool.get()) {
            children = ftpFileStrategy.getChildren(client, path);
        }
        return new FTPPathDirectoryStream(path, children, filter);
    }

    void createDirectory(FTPPath path, FileAttribute<?>... attrs) throws IOException {
        if (attrs.length > 0) {
            throw Messages.fileSystemProvider().unsupportedCreateFileAttribute(attrs[0].name());
        }
        try (FTPClientPool.Client client = clientPool.get()) {
            client.mkdir(path.path());
        }
    }

    void delete(FTPPath path) throws IOException {
        try (FTPClientPool.Client client = clientPool.get()) {
            FTPFile ftpFile = getFTPFile(client, path);
            boolean isDirectory = ftpFile.isDirectory();
            client.delete(path.path(), isDirectory);
        }
    }

    FTPPath readSymbolicLink(FTPPath path) throws IOException {
        try (FTPClientPool.Client client = clientPool.get()) {
            FTPFile ftpFile = getFTPFile(client, path);
            FTPFile link = getLink(client, ftpFile, path);
            if (link == null) {
                throw new NotLinkException(path.path());
            }
            return path.resolveSibling(link.getLink());
        }
    }

    void copy(FTPPath source, FTPPath target, CopyOption... options) throws IOException {
        boolean sameFileSystem = source.getFileSystem() == target.getFileSystem();
        CopyOptions copyOptions = CopyOptions.forCopy(options);

        try (FTPClientPool.Client client = clientPool.get()) {
            // get the FTP file to determine whether a directory needs to be created or a file needs to be copied
            // Files.copy specifies that for links, the final target must be copied
            FTPPathAndFilePair sourcePair = toRealPath(client, source, true);

            if (!sameFileSystem) {
                copyAcrossFileSystems(client, source, sourcePair.ftpFile, target, copyOptions);
                return;
            }

            try {
                if (sourcePair.ftpPath.path().equals(toRealPath(client, target, true).ftpPath.path())) {
                    // non-op, don't do a thing as specified by Files.copy
                    return;
                }
            } catch (NoSuchFileException e) {
                // the target does not exist or either path is an invalid link, ignore the error and continue
            }

            FTPFile targetFtpFile = findFTPFile(client, target);

            if (targetFtpFile != null) {
                if (copyOptions.replaceExisting) {
                    client.delete(target.path(), targetFtpFile.isDirectory());
                } else {
                    throw new FileAlreadyExistsException(target.path());
                }
            }

            if (sourcePair.ftpFile.isDirectory()) {
                client.mkdir(target.path());
            } else {
                try (FTPClientPool.Client client2 = clientPool.getOrCreate()) {
                    copyFile(client, source, client2, target, copyOptions);
                }
            }
        }
    }

    private void copyAcrossFileSystems(FTPClientPool.Client sourceClient, FTPPath source, FTPFile sourceFtpFile, FTPPath target, CopyOptions options)
            throws IOException {

        try (FTPClientPool.Client targetClient = target.getFileSystem().clientPool.getOrCreate()) {

            FTPFile targetFtpFile = findFTPFile(targetClient, target);

            if (targetFtpFile != null) {
                if (options.replaceExisting) {
                    targetClient.delete(target.path(), targetFtpFile.isDirectory());
                } else {
                    throw new FileAlreadyExistsException(target.path());
                }
            }

            if (sourceFtpFile.isDirectory()) {
                sourceClient.mkdir(target.path());
            } else {
                copyFile(sourceClient, source, targetClient, target, options);
            }
        }
    }

    private void copyFile(FTPClientPool.Client sourceClient, FTPPath source, FTPClientPool.Client targetClient, FTPPath target, CopyOptions options) throws IOException {
        OpenOptions inOptions = OpenOptions.forNewInputStream(options.toOpenOptions(StandardOpenOption.READ));
        OpenOptions outOptions = OpenOptions
                .forNewOutputStream(options.toOpenOptions(StandardOpenOption.WRITE, StandardOpenOption.CREATE));
        try (InputStream in = sourceClient.newInputStream(source.path(), inOptions)) {
            targetClient.storeFile(target.path(), in, outOptions, outOptions.options);
        }
    }

    void move(FTPPath source, FTPPath target, CopyOption... options) throws IOException {
        boolean sameFileSystem = source.getFileSystem() == target.getFileSystem();
        CopyOptions copyOptions = CopyOptions.forMove(sameFileSystem, options);

        try (FTPClientPool.Client client = clientPool.get()) {
            if (!sameFileSystem) {
                FTPFile ftpFile = getFTPFile(client, source);
                if (getLink(client, ftpFile, source) != null) {
                    throw new IOException(FTPMessages.copyOfSymbolicLinksAcrossFileSystemsNotSupported());
                }
                copyAcrossFileSystems(client, source, ftpFile, target, copyOptions);
                client.delete(source.path(), ftpFile.isDirectory());
                return;
            }

            try {
                if (isSameFile(client, source, target)) {
                    // non-op, don't do a thing as specified by Files.move
                    return;
                }
            } catch (NoSuchFileException e) {
                // the source or target does not exist or either path is an invalid link
                // call getFTPFile to ensure the source file exists
                // ignore any error to target or if the source link is invalid
                getFTPFile(client, source);
            }

            if (toAbsolutePath(source).parentPath() == null) {
                // cannot move or rename the root
                throw new DirectoryNotEmptyException(source.path());
            }

            FTPFile targetFTPFile = findFTPFile(client, target);
            if (copyOptions.replaceExisting && targetFTPFile != null) {
                client.delete(target.path(), targetFTPFile.isDirectory());
            }

            client.rename(source.path(), target.path());
        }
    }

    boolean isSameFile(FTPPath path, FTPPath path2) throws IOException {
        if (path.getFileSystem() != path2.getFileSystem()) {
            return false;
        }
        if (path.equals(path2)) {
            return true;
        }
        try (FTPClientPool.Client client = clientPool.get()) {
            return isSameFile(client, path, path2);
        }
    }

    private boolean isSameFile(FTPClientPool.Client client, FTPPath path, FTPPath path2) throws IOException {
        if (path.equals(path2)) {
            return true;
        }
        return toRealPath(client, path, true).ftpPath.path().equals(toRealPath(client, path2, true).ftpPath.path());
    }

    boolean isHidden(FTPPath path) throws IOException {
        // call getFTPFile to check for existence
        try (FTPClientPool.Client client = clientPool.get()) {
            getFTPFile(client, path);
        }
        String fileName = path.fileName();
        return !CURRENT_DIR.equals(fileName) && !PARENT_DIR.equals(fileName) && fileName.startsWith(".");
    }

    FileStore getFileStore(FTPPath path) throws IOException {
        // call getFTPFile to check existence of the path
        try (FTPClientPool.Client client = clientPool.get()) {
            getFTPFile(client, path);
        }
        return fileStore;
    }

    void checkAccess(FTPPath path, AccessMode... modes) throws IOException {
        try (FTPClientPool.Client client = clientPool.get()) {
            FTPFile ftpFile = getFTPFile(client, path);
            for (AccessMode mode : modes) {
                if (!hasAccess(ftpFile, mode)) {
                    throw new AccessDeniedException(path.path());
                }
            }
        }
    }

    private boolean hasAccess(FTPFile ftpFile, AccessMode mode) {
        switch (mode) {
            case READ:
                return ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION);
            case WRITE:
                return ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION);
            case EXECUTE:
                return ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION);
            default:
                return false;
        }
    }

    PosixFileAttributes readAttributes(FTPPath path, LinkOption... options) throws IOException {
        boolean followLinks = LinkOptionSupport.followLinks(options);
        try (FTPClientPool.Client client = clientPool.get()) {
            FTPPathAndFilePair pair = toRealPath(client, path, followLinks);
            ZonedDateTime lastModified = client.mdtm(pair.ftpPath.path());
            FTPFile link = followLinks ? null : getLink(client, pair.ftpFile, path);
            FTPFile ftpFile = link == null ? pair.ftpFile : link;
            return new FTPPathFileAttributes(ftpFile, lastModified);
        }
    }

    Map<String, Object> readAttributes(FTPPath path, String attributes, LinkOption... options) throws IOException {
        String view;
        int pos = attributes.indexOf(':');
        if (pos == -1) {
            view = "basic";
            attributes = "basic:" + attributes;
        } else {
            view = attributes.substring(0, pos);
        }
        if (!SUPPORTED_FILE_ATTRIBUTE_VIEWS.contains(view)) {
            throw Messages.fileSystemProvider().unsupportedFileAttributeView(view);
        }

        Set<String> allowedAttributes;
        if (attributes.startsWith("basic:")) {
            allowedAttributes = BASIC_ATTRIBUTES;
        } else if (attributes.startsWith("owner:")) {
            allowedAttributes = OWNER_ATTRIBUTES;
        } else if (attributes.startsWith("posix:")) {
            allowedAttributes = POSIX_ATTRIBUTES;
        } else {
            // should not occur
            throw Messages.fileSystemProvider().unsupportedFileAttributeView(attributes.substring(0, attributes.indexOf(':')));
        }

        Map<String, Object> result = getAttributeMap(attributes, allowedAttributes);

        PosixFileAttributes posixAttributes = readAttributes(path, options);

        for (Map.Entry<String, Object> entry : result.entrySet()) {
            switch (entry.getKey()) {
                case "basic:lastModifiedTime":
                case "posix:lastModifiedTime":
                    entry.setValue(posixAttributes.lastModifiedTime());
                    break;
                case "basic:lastAccessTime":
                case "posix:lastAccessTime":
                    entry.setValue(posixAttributes.lastAccessTime());
                    break;
                case "basic:creationTime":
                case "posix:creationTime":
                    entry.setValue(posixAttributes.creationTime());
                    break;
                case "basic:size":
                case "posix:size":
                    entry.setValue(posixAttributes.size());
                    break;
                case "basic:isRegularFile":
                case "posix:isRegularFile":
                    entry.setValue(posixAttributes.isRegularFile());
                    break;
                case "basic:isDirectory":
                case "posix:isDirectory":
                    entry.setValue(posixAttributes.isDirectory());
                    break;
                case "basic:isSymbolicLink":
                case "posix:isSymbolicLink":
                    entry.setValue(posixAttributes.isSymbolicLink());
                    break;
                case "basic:isOther":
                case "posix:isOther":
                    entry.setValue(posixAttributes.isOther());
                    break;
                case "basic:fileKey":
                case "posix:fileKey":
                    entry.setValue(posixAttributes.fileKey());
                    break;
                case "owner:owner":
                case "posix:owner":
                    entry.setValue(posixAttributes.owner());
                    break;
                case "posix:group":
                    entry.setValue(posixAttributes.group());
                    break;
                case "posix:permissions":
                    entry.setValue(posixAttributes.permissions());
                    break;
                default:
                    // should not occur
                    throw new IllegalStateException("unexpected attribute name: " + entry.getKey());
            }
        }
        return result;
    }

    private Map<String, Object> getAttributeMap(String attributes, Set<String> allowedAttributes) {
        int indexOfColon = attributes.indexOf(':');
        String prefix = attributes.substring(0, indexOfColon + 1);
        attributes = attributes.substring(indexOfColon + 1);

        String[] attributeList = attributes.split(",");
        Map<String, Object> result = new HashMap<>(allowedAttributes.size());

        for (String attribute : attributeList) {
            String prefixedAttribute = prefix + attribute;
            if (allowedAttributes.contains(prefixedAttribute)) {
                result.put(prefixedAttribute, null);
            } else if ("*".equals(attribute)) {
                for (String s : allowedAttributes) {
                    result.put(s, null);
                }
            } else {
                throw Messages.fileSystemProvider().unsupportedFileAttribute(attribute);
            }
        }
        return result;
    }

    FTPFile getFTPFile(FTPPath path) throws IOException {
        try (FTPClientPool.Client client = clientPool.get()) {
            return getFTPFile(client, path);
        }
    }

    private FTPFile getFTPFile(FTPClientPool.Client client, FTPPath path) throws IOException {
        return ftpFileStrategy.getFTPFile(client, path);
    }

    private FTPFile findFTPFile(FTPClientPool.Client client, FTPPath path) throws IOException {
        try {
            return getFTPFile(client, path);
        } catch (NoSuchFileException e) {
            return null;
        }
    }

    private FTPFile getLink(FTPClientPool.Client client, FTPFile ftpFile, FTPPath path) throws IOException {
        return ftpFileStrategy.getLink(client, ftpFile, path);
    }

    long getTotalSpace() {
        // FTPClient does not support retrieving the total space
        return Long.MAX_VALUE;
    }

    long getUsableSpace() {
        // FTPClient does not support retrieving the usable space
        return Long.MAX_VALUE;
    }

    long getUnallocatedSpace() {
        // FTPClient does not support retrieving the unallocated space
        return Long.MAX_VALUE;
    }

    private static final class FTPPathAndFilePair {
        private final FTPPath ftpPath;
        private final FTPFile ftpFile;

        private FTPPathAndFilePair(FTPPath ftpPath, FTPFile ftpFile) {
            this.ftpPath = ftpPath;
            this.ftpFile = ftpFile;
        }
    }

    private static final class FTPFileAndOutputStreamPair {

        private final FTPFile ftpFile;
        private final OutputStream out;

        private FTPFileAndOutputStreamPair(FTPFile ftpFile, OutputStream out) {
            this.ftpFile = ftpFile;
            this.out = out;
        }
    }

    private static final class FTPPathDirectoryStream extends AbstractDirectoryStream<Path> {

        private final FTPPath path;
        private final List<FTPFile> files;
        private Iterator<FTPFile> iterator;

        private FTPPathDirectoryStream(FTPPath path, List<FTPFile> files, Filter<? super Path> filter) {
            super(filter);
            this.path = path;
            this.files = files;
        }

        @Override
        protected void setupIteration() {
            iterator = files.iterator();
        }

        @Override
        protected Path getNext() throws IOException {
            return iterator.hasNext() ? path.resolve(getFileName(iterator.next())) : null;
        }
    }

    private static final class FTPPathFileAttributes implements PosixFileAttributes {

        private static final FileTime EPOCH = FileTime.fromMillis(0L);

        private final FTPFile ftpFile;
        private final FileTime lastModified;

        private FTPPathFileAttributes(FTPFile ftpFile, ZonedDateTime lastModified) {
            this.ftpFile = ftpFile;
            if (lastModified == null) {
                ZonedDateTime timestamp = ftpFile.getTimestamp();
                this.lastModified = timestamp == null ? EPOCH : FileTime.from(timestamp.toInstant());
            } else {
                this.lastModified = FileTime.from(lastModified.toInstant());
            }
        }

        @Override
        public UserPrincipal owner() {
            String user = ftpFile.getUser();
            return user == null ? null : new SimpleUserPrincipal(user);
        }

        @Override
        public GroupPrincipal group() {
            String group = ftpFile.getGroup();
            return group == null ? null : new SimpleGroupPrincipal(group);
        }

        @Override
        public Set<PosixFilePermission> permissions() {
            Set<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);
            addPermissionIfSet(ftpFile, FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, PosixFilePermission.OWNER_READ, permissions);
            addPermissionIfSet(ftpFile, FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, PosixFilePermission.OWNER_WRITE, permissions);
            addPermissionIfSet(ftpFile, FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, PosixFilePermission.OWNER_EXECUTE, permissions);
            addPermissionIfSet(ftpFile, FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION, PosixFilePermission.GROUP_READ, permissions);
            addPermissionIfSet(ftpFile, FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION, PosixFilePermission.GROUP_WRITE, permissions);
            addPermissionIfSet(ftpFile, FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION, PosixFilePermission.GROUP_EXECUTE, permissions);
            addPermissionIfSet(ftpFile, FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, PosixFilePermission.OTHERS_READ, permissions);
            addPermissionIfSet(ftpFile, FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION, PosixFilePermission.OTHERS_WRITE, permissions);
            addPermissionIfSet(ftpFile, FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION, PosixFilePermission.OTHERS_EXECUTE, permissions);
            return permissions;
        }

        private void addPermissionIfSet(FTPFile ftpFile, int access, int permission, PosixFilePermission value,
                                        Set<PosixFilePermission> permissions) {

            if (ftpFile.hasPermission(access, permission)) {
                permissions.add(value);
            }
        }

        @Override
        public FileTime lastModifiedTime() {
            return lastModified;
        }

        @Override
        public FileTime lastAccessTime() {
            return lastModifiedTime();
        }

        @Override
        public FileTime creationTime() {
            return lastModifiedTime();
        }

        @Override
        public boolean isRegularFile() {
            return ftpFile.isFile();
        }

        @Override
        public boolean isDirectory() {
            return ftpFile.isDirectory();
        }

        @Override
        public boolean isSymbolicLink() {
            return ftpFile.isSymbolicLink();
        }

        @Override
        public boolean isOther() {
            return false;
        }

        @Override
        public long size() {
            return ftpFile.getSize();
        }

        @Override
        public Object fileKey() {
            return null;
        }
    }
}
