package org.xbib.io.ftp.fs;

/**
 * The possible FTPS security modes.
 */
public enum SecurityMode {
    /**
     * Indicates <a href="https://en.wikipedia.org/wiki/FTPS#Implicit">implicit</a> security should be used.
     */
    IMPLICIT(true),
    /**
     * Indicates <a href="https://en.wikipedia.org/wiki/FTPS#Explicit">explicit</a> security should be used.
     */
    EXPLICIT(false),;

    final boolean isImplicit;

    SecurityMode(boolean isImplicit) {
        this.isImplicit = isImplicit;
    }
}
