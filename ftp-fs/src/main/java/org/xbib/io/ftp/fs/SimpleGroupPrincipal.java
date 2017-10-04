package org.xbib.io.ftp.fs;

import java.nio.file.attribute.GroupPrincipal;

/**
 * A {@link GroupPrincipal} implementation that simply stores a name.
 */
public class SimpleGroupPrincipal extends SimpleUserPrincipal implements GroupPrincipal {

    /**
     * Creates a new group principal.
     *
     * @param name The name of the group principal.
     */
    public SimpleGroupPrincipal(String name) {
        super(name);
    }
}
