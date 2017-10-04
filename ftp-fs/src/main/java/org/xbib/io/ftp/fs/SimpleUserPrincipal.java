package org.xbib.io.ftp.fs;

import java.nio.file.attribute.UserPrincipal;
import java.util.Objects;

/**
 * A {@link UserPrincipal} implementation that simply stores a name.
 */
public class SimpleUserPrincipal implements UserPrincipal {

    private final String name;

    /**
     * Creates a new user principal.
     *
     * @param name The name of the user principal.
     */
    public SimpleUserPrincipal(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        SimpleUserPrincipal other = (SimpleUserPrincipal) o;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }
}
