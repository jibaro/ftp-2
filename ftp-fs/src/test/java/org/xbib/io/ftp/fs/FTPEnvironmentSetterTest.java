package org.xbib.io.ftp.fs;

import org.xbib.io.ftp.client.FTPClient;
import org.xbib.io.ftp.client.FTPClientConfig;
import org.xbib.io.ftp.client.parser.DefaultFTPFileEntryParserFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FTPEnvironmentSetterTest {

    private final Method setter;
    private final String propertyName;
    private final Object propertyValue;

    public FTPEnvironmentSetterTest(String methodName, String propertyName, Object propertyValue) {
        this.setter = findMethod(methodName);
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    @Parameters(name = "{0}")
    public static List<Object[]> getParameters() {
        Object[][] parameters = {
                {"withSoTimeout", "soTimeout", 1000,},
                {"withSendBufferSize", "sendBufferSize", 4096,},
                {"withReceiveBufferSize", "receiveBufferSize", 2048,},
                {"withTcpNoDelay", "tcpNoDelay", true,},
                {"withKeepAlive", "keepAlive", true,},
                {"withSocketFactory", "socketFactory", SocketFactory.getDefault(),},
                {"withServerSocketFactory", "serverSocketFactory", ServerSocketFactory.getDefault(),},
                {"withConnectTimeout", "connectTimeout", 1000,},
                {"withProxy", "proxy", new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 21)),},
                {"withCharset", "charset", StandardCharsets.UTF_8,},
                {"withControlEncoding", "controlEncoding", "UTF-8",},
                {"withStrictlyMultilineParsing", "strictMultilineParsing", true,},
                {"withDataTimeout", "dataTimeout", 1000,},
                {"withParserFactory", "parserFactory", new DefaultFTPFileEntryParserFactory(),},
                {"withRemoteVerificationEnabled", "remoteVerificationEnabled", true,},
                {"withDefaultDirectory", "defaultDir", "/",},
                {"withConnectionMode", "connectionMode", ConnectionMode.PASSIVE,},
                {"withActiveExternalIPAddress", "activeExternalIPAddress", "127.0.0.1",},
                {"withPassiveLocalIPAddress", "passiveLocalIPAddress", "127.0.0.1",},
                {"withReportActiveExternalIPAddress", "reportActiveExternalIPAddress", "127.0.0.1",},
                {"withBufferSize", "bufferSize", 1000,},
                {"withSendDataSocketBufferSize", "sendDataSocketBufferSize", 1024,},
                {"withReceiveDataSocketBufferSize", "receiveDataSocketBufferSize", 2048,},
                {"withClientConfig", "clientConfig", new FTPClientConfig(),},
                {"withUseEPSVwithIPv4", "useEPSVwithIPv4", true,},
                {"withControlKeepAliveTimeout", "controlKeepAliveTimeout", 1000L,},
                {"withControlKeepAliveReplyTimeout", "controlKeepAliveReplyTimeout", 1000,},
                {"withPassiveNatWorkaroundStrategy", "passiveNatWorkaroundStrategy", new FTPClient.NatServerResolverImpl(new FTPClient()),},
                {"withAutodetectEncoding", "autodetectEncoding", true,},
                {"withClientConnectionCount", "clientConnectionCount", 5,},
                {"withFileSystemExceptionFactory", "fileSystemExceptionFactory", DefaultFileSystemExceptionFactory.INSTANCE,}
        };
        return Arrays.asList(parameters);
    }

    private Method findMethod(String methodName) {
        for (Method method : FTPEnvironment.class.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == 1) {
                return method;
            }
        }
        throw new AssertionError("Could not find method " + methodName);
    }

    @Test
    public void testSetter() throws ReflectiveOperationException {
        FTPEnvironment env = new FTPEnvironment();

        assertEquals(Collections.emptyMap(), env);

        setter.invoke(env, propertyValue);

        assertEquals(Collections.singletonMap(propertyName, propertyValue), env);
    }
}
