package org.xbib.io.ftp.fs;

import java.nio.file.attribute.FileAttribute;
import java.util.Objects;

/**
 * A simple file attribute implementation.
 */
public class SimpleFileAttribute<T> implements FileAttribute<T> {

    private final String name;
    private final T value;

    /**
     * Creates a new file attribute.
     *
     * @param name  The attribute name.
     * @param value The attribute value.
     * @throws NullPointerException If the name or value is {@code null}.
     */
    public SimpleFileAttribute(String name, T value) {
        this.name = Objects.requireNonNull(name);
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        SimpleFileAttribute<?> other = (SimpleFileAttribute<?>) o;
        return name.equals(other.name)
                && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        int hash = name.hashCode();
        hash = 31 * hash + value.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "[name=" + name
                + ",value=" + value
                + "]";
    }
}
