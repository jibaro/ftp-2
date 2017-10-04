package org.xbib.io.ftp.client;

import java.util.Objects;

/**
 * Implements some simple FTPFileFilter classes.
 */
public class FTPFileFilters {

    /**
     * Accepts all FTPFile entries, including null.
     */
    public static final FTPFileFilter ALL = file -> true;

    /**
     * Accepts all non-null FTPFile entries.
     */
    public static final FTPFileFilter NON_NULL = Objects::nonNull;

    /**
     * Accepts all (non-null) FTPFile directory entries.
     */
    public static final FTPFileFilter DIRECTORIES = file -> file != null && file.isDirectory();

}
