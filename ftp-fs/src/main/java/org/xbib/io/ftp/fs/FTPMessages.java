package org.xbib.io.ftp.fs;

import java.util.ResourceBundle;

/**
 * A utility class for providing translated messages and exceptions.
 */
final class FTPMessages {

    private static final String BUNDLE_NAME = "org.xbib.ftp.fs.messages";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, UTF8Control.INSTANCE);

    private FTPMessages() {
        throw new Error("cannot create instances of " + getClass().getName());
    }

    private static synchronized String getMessage(String key) {
        return BUNDLE.getString(key);
    }

    public static String copyOfSymbolicLinksAcrossFileSystemsNotSupported() {
        return getMessage("copyOfSymbolicLinksAcrossFileSystemsNotSupported");
    }
}
