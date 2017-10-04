package org.xbib.io.ftp.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * A resource bundle control that uses UTF-8 instead of the default encoding when reading resources from properties files. It is thread-safe.
 */
public final class UTF8Control extends Control {

    /**
     * The single instance.
     */
    public static final UTF8Control INSTANCE = new UTF8Control();

    private UTF8Control() {
        super();
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, final ClassLoader loader, final boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        if (!"java.properties".equals(format)) {
            return super.newBundle(baseName, locale, format, loader, reload);
        }
        String bundleName = toBundleName(baseName, locale);
        ResourceBundle bundle = null;
        final String resourceName = toResourceName(bundleName, "properties");
        InputStream in = null;
        try {
            in = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                @Override
                public InputStream run() throws Exception {
                    InputStream in = null;
                    if (reload) {
                        URL url = loader.getResource(resourceName);
                        if (url != null) {
                            URLConnection connection = url.openConnection();
                            if (connection != null) {
                                // Disable caches to get fresh data for reloading.
                                connection.setUseCaches(false);
                                in = connection.getInputStream();
                            }
                        }
                    } else {
                        in = loader.getResourceAsStream(resourceName);
                    }
                    return in;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
        if (in != null) {
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                bundle = new PropertyResourceBundle(reader);
            }
        }
        return bundle;
    }
}
