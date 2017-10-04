package org.xbib.groovy.ftp;

/**
 *
 * @param <T> the context parameter
 */
public interface WithContext<T> {
    T perform(FTPContext ctx) throws Exception;
}
