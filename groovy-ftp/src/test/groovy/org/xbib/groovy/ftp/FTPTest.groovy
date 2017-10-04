package org.xbib.groovy.ftp

import groovy.util.logging.Log4j2
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path

@Log4j2
class FTPTest {

    @Test
    void testFTP() {
        FTP ftp = FTP.newInstance("ftp://demo.wftpserver.com:21", [username: 'demo-user', password: 'demo-user'.toCharArray()])
        log.info ftp.exists('/')
        ftp.each('/') { Path path ->
            log.info "{} {} {}", path, Files.isDirectory(path), Files.getLastModifiedTime(path)
        }
    }
}
