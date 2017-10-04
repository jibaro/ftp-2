package org.xbib.io.ftp.client;

/**
 * Perform filtering on FTPFile entries.
 */
public interface FTPFileFilter {
    /**
     * Checks if an FTPFile entry should be included or not.
     *
     * @param file entry to be checked for inclusion. May be <code>null</code>.
     * @return <code>true</code> if the file is to be included, <code>false</code> otherwise
     */
    boolean accept(FTPFile file);
}
