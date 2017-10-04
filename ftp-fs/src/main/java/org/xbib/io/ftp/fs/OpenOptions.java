package org.xbib.io.ftp.fs;

import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A representation of possible open options.
 */
final class OpenOptions extends TransferOptions {

    public final boolean read;
    public final boolean write;
    public final boolean append;
    public final boolean create;
    public final boolean createNew;
    public final boolean deleteOnClose;

    public final Collection<? extends OpenOption> options;

    private OpenOptions(boolean read, boolean write, boolean append, boolean create, boolean createNew, boolean deleteOnClose,
                        FileType fileType, FileStructure fileStructure, FileTransferMode fileTransferMode,
                        Collection<? extends OpenOption> options) {

        super(fileType, fileStructure, fileTransferMode);
        this.read = read;
        this.write = write;
        this.append = append;
        this.create = create;
        this.createNew = createNew;
        this.deleteOnClose = deleteOnClose;

        this.options = options;
    }

    static OpenOptions forNewInputStream(OpenOption... options) {
        return forNewInputStream(Arrays.asList(options));
    }

    static OpenOptions forNewInputStream(Collection<? extends OpenOption> options) {
        if (options.isEmpty()) {
            return new OpenOptions(true, false, false, false, false, false, null, null, null, Collections.<OpenOption>emptySet());
        }

        boolean deleteOnClose = false;
        FileType fileType = null;
        FileStructure fileStructure = null;
        FileTransferMode fileTransferMode = null;

        for (OpenOption option : options) {
            if (option == StandardOpenOption.DELETE_ON_CLOSE) {
                deleteOnClose = true;
            } else if (option instanceof FileType) {
                fileType = setOnce((FileType) option, fileType, options);
            } else if (option instanceof FileStructure) {
                fileStructure = setOnce((FileStructure) option, fileStructure, options);
            } else if (option instanceof FileTransferMode) {
                fileTransferMode = setOnce((FileTransferMode) option, fileTransferMode, options);
            } else if (option != StandardOpenOption.READ && option != StandardOpenOption.TRUNCATE_EXISTING && !isIgnoredOpenOption(option)) {
                // TRUNCATE_EXISTING is ignored in combination with READ
                throw Messages.fileSystemProvider().unsupportedOpenOption(option);
            }
        }

        return new OpenOptions(true, false, false, false, false, deleteOnClose, fileType, fileStructure, fileTransferMode, options);
    }

    static OpenOptions forNewOutputStream(OpenOption... options) {
        return forNewOutputStream(Arrays.asList(options));
    }

    static OpenOptions forNewOutputStream(Collection<? extends OpenOption> options) {
        if (options.isEmpty()) {
            // CREATE, TRUNCATE_EXISTING and WRITE, i.e. create, not createNew, and not append
            return new OpenOptions(false, true, false, true, false, false, null, null, null, Collections.<OpenOption>emptySet());
        }

        boolean append = false;
        boolean truncateExisting = false;
        boolean create = false;
        boolean createNew = false;
        boolean deleteOnClose = false;
        FileType fileType = null;
        FileStructure fileStructure = null;
        FileTransferMode fileTransferMode = null;

        for (OpenOption option : options) {
            if (option == StandardOpenOption.APPEND) {
                append = true;
            } else if (option == StandardOpenOption.TRUNCATE_EXISTING) {
                truncateExisting = true;
            } else if (option == StandardOpenOption.CREATE) {
                create = true;
            } else if (option == StandardOpenOption.CREATE_NEW) {
                createNew = true;
            } else if (option == StandardOpenOption.DELETE_ON_CLOSE) {
                deleteOnClose = true;
            } else if (option instanceof FileType) {
                fileType = setOnce((FileType) option, fileType, options);
            } else if (option instanceof FileStructure) {
                fileStructure = setOnce((FileStructure) option, fileStructure, options);
            } else if (option instanceof FileTransferMode) {
                fileTransferMode = setOnce((FileTransferMode) option, fileTransferMode, options);
            } else if (option != StandardOpenOption.WRITE && !isIgnoredOpenOption(option)) {
                throw Messages.fileSystemProvider().unsupportedOpenOption(option);
            }
        }

        // append and truncateExisting contradict each other
        if (append && truncateExisting) {
            throw Messages.fileSystemProvider().illegalOpenOptionCombination(options);
        }

        return new OpenOptions(false, true, append, create, createNew, deleteOnClose, fileType, fileStructure, fileTransferMode, options);
    }

    static OpenOptions forNewByteChannel(Set<? extends OpenOption> options) {

        boolean read = false;
        boolean write = false;
        boolean append = false;
        boolean truncateExisting = false;
        boolean create = false;
        boolean createNew = false;
        boolean deleteOnClose = false;
        FileType fileType = null;
        FileStructure fileStructure = null;
        FileTransferMode fileTransferMode = null;

        for (OpenOption option : options) {
            if (option == StandardOpenOption.READ) {
                read = true;
            } else if (option == StandardOpenOption.WRITE) {
                write = true;
            } else if (option == StandardOpenOption.APPEND) {
                append = true;
            } else if (option == StandardOpenOption.TRUNCATE_EXISTING) {
                truncateExisting = true;
            } else if (option == StandardOpenOption.CREATE) {
                create = true;
            } else if (option == StandardOpenOption.CREATE_NEW) {
                createNew = true;
            } else if (option == StandardOpenOption.DELETE_ON_CLOSE) {
                deleteOnClose = true;
            } else if (option instanceof FileType) {
                fileType = setOnce((FileType) option, fileType, options);
            } else if (option instanceof FileStructure) {
                fileStructure = setOnce((FileStructure) option, fileStructure, options);
            } else if (option instanceof FileTransferMode) {
                fileTransferMode = setOnce((FileTransferMode) option, fileTransferMode, options);
            } else if (!isIgnoredOpenOption(option)) {
                throw Messages.fileSystemProvider().unsupportedOpenOption(option);
            }
        }

        // as per Files.newByteChannel, if none of these options is given, default to read
        if (!read && !write && !append) {
            read = true;
        }

        // read contradicts with write, append, create and createNew; TRUNCATE_EXISTING is ignored in combination with READ
        if (read && (write || append || create || createNew)) {
            throw Messages.fileSystemProvider().illegalOpenOptionCombination(options);
        }

        // append and truncateExisting contract each other
        if (append && truncateExisting) {
            throw Messages.fileSystemProvider().illegalOpenOptionCombination(options);
        }

        // read and write contract each other; read and append contract each other; append and truncateExisting contract each other
        if ((read && write) || (read && append) || (append && truncateExisting)) {
            throw Messages.fileSystemProvider().illegalOpenOptionCombination(options);
        }

        return new OpenOptions(read, write, append, create, createNew, deleteOnClose, fileType, fileStructure, fileTransferMode, options);
    }

    static <T> T setOnce(T newValue, T existing, Collection<? extends OpenOption> options) {
        if (existing != null && !existing.equals(newValue)) {
            throw Messages.fileSystemProvider().illegalOpenOptionCombination(options);
        }
        return newValue;
    }

    private static boolean isIgnoredOpenOption(OpenOption option) {
        return option == StandardOpenOption.SPARSE
                || option == StandardOpenOption.SYNC
                || option == StandardOpenOption.DSYNC
                || option == LinkOption.NOFOLLOW_LINKS;
    }
}
