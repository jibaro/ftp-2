package org.xbib.io.ftp.fs;

import java.nio.file.CopyOption;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A representation of possible copy options.
 */
final class CopyOptions extends TransferOptions {

    public final boolean replaceExisting;

    public final Collection<? extends CopyOption> options;

    private CopyOptions(boolean replaceExisting,
                        FileType fileType, FileStructure fileStructure, FileTransferMode fileTransferMode,
                        Collection<? extends CopyOption> options) {

        super(fileType, fileStructure, fileTransferMode);
        this.replaceExisting = replaceExisting;

        this.options = options;
    }

    static CopyOptions forCopy(CopyOption... options) {

        boolean replaceExisting = false;
        FileType fileType = null;
        FileStructure fileStructure = null;
        FileTransferMode fileTransferMode = null;

        for (CopyOption option : options) {
            if (option == StandardCopyOption.REPLACE_EXISTING) {
                replaceExisting = true;
            } else if (option instanceof FileType) {
                fileType = setOnce((FileType) option, fileType, options);
            } else if (option instanceof FileStructure) {
                fileStructure = setOnce((FileStructure) option, fileStructure, options);
            } else if (option instanceof FileTransferMode) {
                fileTransferMode = setOnce((FileTransferMode) option, fileTransferMode, options);
            } else if (!isIgnoredCopyOption(option)) {
                throw Messages.fileSystemProvider().unsupportedCopyOption(option);
            }
        }

        return new CopyOptions(replaceExisting, fileType, fileStructure, fileTransferMode, Arrays.asList(options));
    }

    static CopyOptions forMove(boolean sameFileSystem, CopyOption... options) {

        boolean replaceExisting = false;
        FileType fileType = null;
        FileStructure fileStructure = null;
        FileTransferMode fileTransferMode = null;

        for (CopyOption option : options) {
            if (option == StandardCopyOption.REPLACE_EXISTING) {
                replaceExisting = true;
            } else if (option instanceof FileType) {
                fileType = setOnce((FileType) option, fileType, options);
            } else if (option instanceof FileStructure) {
                fileStructure = setOnce((FileStructure) option, fileStructure, options);
            } else if (option instanceof FileTransferMode) {
                fileTransferMode = setOnce((FileTransferMode) option, fileTransferMode, options);
            } else if (!(option == StandardCopyOption.ATOMIC_MOVE && sameFileSystem) && !isIgnoredCopyOption(option)) {
                throw Messages.fileSystemProvider().unsupportedCopyOption(option);
            }
        }

        return new CopyOptions(replaceExisting, fileType, fileStructure, fileTransferMode, Arrays.asList(options));
    }

    private static <T> T setOnce(T newValue, T existing, CopyOption... options) {
        if (existing != null && !existing.equals(newValue)) {
            throw Messages.fileSystemProvider().illegalCopyOptionCombination(options);
        }
        return newValue;
    }

    private static boolean isIgnoredCopyOption(CopyOption option) {
        return option == LinkOption.NOFOLLOW_LINKS;
    }

    public Collection<OpenOption> toOpenOptions(OpenOption... additional) {
        List<OpenOption> openOptions = new ArrayList<>(options.size() + additional.length);
        for (CopyOption option : options) {
            if (option instanceof OpenOption) {
                openOptions.add((OpenOption) option);
            }
        }
        Collections.addAll(openOptions, additional);
        return openOptions;
    }
}
