package org.xbib.io.ftp.fs;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class FTPEnvironmentTest {

    @Test
    public void testWithLocalAddress() throws UnknownHostException {
        FTPEnvironment env = new FTPEnvironment();

        assertEquals(Collections.emptyMap(), env);

        InetAddress localAddr = InetAddress.getLocalHost();
        int localPort = 21;

        env.withLocalAddress(localAddr, localPort);

        Map<String, Object> expected = new HashMap<>();
        expected.put("localAddr", localAddr);
        expected.put("localPort", localPort);
        assertEquals(expected, env);
    }

    @Test
    public void testWithCredentialsWithoutAccount() {
        FTPEnvironment env = new FTPEnvironment();

        assertEquals(Collections.emptyMap(), env);

        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();

        env.withCredentials(username, password);

        Map<String, Object> expected = new HashMap<>();
        expected.put("username", username);
        expected.put("password", password);
        assertEquals(expected, env);
    }

    @Test
    public void testWithCredentialsWithAccount() {
        FTPEnvironment env = new FTPEnvironment();

        assertEquals(Collections.emptyMap(), env);

        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        String account = UUID.randomUUID().toString();

        env.withCredentials(username, password, account);

        Map<String, Object> expected = new HashMap<>();
        expected.put("username", username);
        expected.put("password", password);
        expected.put("account", account);
        assertEquals(expected, env);
    }

    @Test
    public void testWithSoLinger() {
        FTPEnvironment env = new FTPEnvironment();

        assertEquals(Collections.emptyMap(), env);

        boolean on = true;
        int linger = 5000;

        env.withSoLinger(on, linger);

        Map<String, Object> expected = new HashMap<>();
        expected.put("soLinger.on", on);
        expected.put("soLinger.val", linger);
        assertEquals(expected, env);
    }

    @Test
    public void testWithActivePortRange() {
        FTPEnvironment env = new FTPEnvironment();

        assertEquals(Collections.emptyMap(), env);

        int minPort = 1234;
        int maxPort = 5678;

        env.withActivePortRange(minPort, maxPort);

        Map<String, Object> expected = new HashMap<>();
        expected.put("activePortRange.min", minPort);
        expected.put("activePortRange.max", maxPort);
        assertEquals(expected, env);
    }
}
