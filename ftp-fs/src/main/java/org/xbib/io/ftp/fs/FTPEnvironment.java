package org.xbib.io.ftp.fs;

import org.xbib.io.ftp.client.FTP;
import org.xbib.io.ftp.client.FTPClient;
import org.xbib.io.ftp.client.FTPClientConfig;
import org.xbib.io.ftp.client.FTPFileEntryParser;
import org.xbib.io.ftp.client.parser.FTPFileEntryParserFactory;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A utility class to set up environments that can be used in the {@link FileSystemProvider#newFileSystem(URI, Map)}
 * and {@link FileSystemProvider#newFileSystem(Path, Map)} methods of {@link FTPFileSystemProvider}.
 */
public class FTPEnvironment implements Map<String, Object>, Cloneable {

    // connect support

    private static final String LOCAL_ADDR = "localAddr";
    private static final String LOCAL_PORT = "localPort";

    // login support

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String ACCOUNT = "account";

    // SocketClient

    private static final String SO_TIMEOUT = "soTimeout";
    private static final String SEND_BUFFER_SIZE = "sendBufferSize";
    private static final String RECEIVE_BUFFER_SIZE = "receiveBufferSize";
    private static final String TCP_NO_DELAY = "tcpNoDelay";
    private static final String KEEP_ALIVE = "keepAlive";
    private static final String SO_LINGER_ON = "soLinger.on";
    private static final String SO_LINGER_VALUE = "soLinger.val";
    private static final String SOCKET_FACTORY = "socketFactory";
    private static final String SERVER_SOCKET_FACTORY = "serverSocketFactory";
    private static final String CONNECT_TIMEOUT = "connectTimeout";
    private static final String PROXY = "proxy";
    private static final String CHARSET = "charset";

    // FTP

    private static final String CONTROL_ENCODING = "controlEncoding";
    private static final String STRICT_MULTILINE_PARSING = "strictMultilineParsing";

    // FTPClient

    private static final String DATA_TIMEOUT = "dataTimeout";
    private static final String PARSER_FACTORY = "parserFactory";
    private static final String REMOTE_VERIFICATION_ENABLED = "remoteVerificationEnabled";
    private static final String DEFAULT_DIR = "defaultDir";
    private static final String CONNECTION_MODE = "connectionMode";
    private static final String ACTIVE_PORT_RANGE_MIN = "activePortRange.min";
    private static final String ACTIVE_PORT_RANGE_MAX = "activePortRange.max";
    private static final String ACTIVE_EXTERNAL_IP_ADDRESS = "activeExternalIPAddress";
    private static final String PASSIVE_LOCAL_IP_ADDRESS = "passiveLocalIPAddress";
    private static final String REPORT_ACTIVE_EXTERNAL_IP_ADDRESS = "reportActiveExternalIPAddress";
    private static final String BUFFER_SIZE = "bufferSize";
    private static final String SEND_DATA_SOCKET_BUFFER_SIZE = "sendDataSocketBufferSize";
    private static final String RECEIVE_DATA_SOCKET_BUFFER_SIZE = "receiveDataSocketBufferSize";
    private static final String CLIENT_CONFIG = "clientConfig";
    private static final String USE_EPSV_WITH_IPV4 = "useEPSVwithIPv4";
    private static final String CONTROL_KEEP_ALIVE_TIMEOUT = "controlKeepAliveTimeout";
    private static final String CONTROL_KEEP_ALIVE_REPLY_TIMEOUT = "controlKeepAliveReplyTimeout";
    private static final String PASSIVE_NAT_WORKAROUND = "passiveNatWorkaround";
    private static final String PASSIVE_NAT_WORKAROUND_STRATEGY = "passiveNatWorkaroundStrategy";
    private static final String AUTODETECT_ENCODING = "autodetectEncoding";

    // FTP file system support

    private static final int DEFAULT_CLIENT_CONNECTION_COUNT = 5;
    private static final String CLIENT_CONNECTION_COUNT = "clientConnectionCount";
    private static final String FILE_SYSTEM_EXCEPTION_FACTORY = "fileSystemExceptionFactory";
    private static final String CALCULATE_ACTUAL_TOTAL_SPACE = "calculateActualTotalSpace";

    private Map<String, Object> map;

    /**
     * Creates a new FTP environment.
     */
    public FTPEnvironment() {
        map = new HashMap<>();
    }

    /**
     * Creates a new FTP environment.
     *
     * @param map The map to wrap.
     */
    public FTPEnvironment(Map<String, Object> map) {
        this.map = Objects.requireNonNull(map);
    }

    @SuppressWarnings("unchecked")
    static FTPEnvironment wrap(Map<String, ?> map) {
        if (map instanceof FTPEnvironment) {
            return (FTPEnvironment) map;
        }
        return new FTPEnvironment((Map<String, Object>) map);
    }

    /**
     * Stores the local address to use.
     *
     * @param localAddr The local address to use.
     * @param localPort The local port to use.
     * @return This object.
     * @see Socket#bind(SocketAddress)
     * @see InetSocketAddress#InetSocketAddress(InetAddress, int)
     */
    public FTPEnvironment withLocalAddress(InetAddress localAddr, int localPort) {
        put(LOCAL_ADDR, localAddr);
        put(LOCAL_PORT, localPort);
        return this;
    }

    // login support

    /**
     * Stores the credentials to use.
     *
     * @param username The username to use.
     * @param password The password to use.
     * @return This object.
     */
    public FTPEnvironment withCredentials(String username, char[] password) {
        put(USERNAME, username);
        put(PASSWORD, password);
        return this;
    }

    /**
     * Stores the credentials to use.
     *
     * @param username The username to use.
     * @param password The password to use.
     * @param account  The account to use.
     * @return This object.
     */
    public FTPEnvironment withCredentials(String username, char[] password, String account) {
        put(USERNAME, username);
        put(PASSWORD, password);
        put(ACCOUNT, account);
        return this;
    }

    // SocketClient

    /**
     * Stores the socket timeout.
     *
     * @param timeout The socket timeout in milliseconds.
     * @return This object.
     * @see Socket#setSoTimeout(int)
     */
    public FTPEnvironment withSoTimeout(int timeout) {
        put(SO_TIMEOUT, timeout);
        return this;
    }

    /**
     * Stores the socket send buffer size to use.
     *
     * @param size The size of the buffer in bytes.
     * @return This object.
     * @see Socket#setSendBufferSize(int)
     */
    public FTPEnvironment withSendBufferSize(int size) {
        put(SEND_BUFFER_SIZE, size);
        return this;
    }

    /**
     * Stores the socket receive buffer size to use.
     *
     * @param size The size of the buffer in bytes.
     * @return This object.
     * @see Socket#setReceiveBufferSize(int)
     */
    public FTPEnvironment withReceiveBufferSize(int size) {
        put(RECEIVE_BUFFER_SIZE, size);
        return this;
    }

    /**
     * Stores whether or not the Nagle's algorithm ({@code TCP_NODELAY}) should be enabled.
     *
     * @param on {@code true} if Nagle's algorithm should be enabled, or {@code false} otherwise.
     * @return This object.
     * @see Socket#setTcpNoDelay(boolean)
     */
    public FTPEnvironment withTcpNoDelay(boolean on) {
        put(TCP_NO_DELAY, on);
        return this;
    }

    /**
     * Stores whether or not {@code SO_KEEPALIVE} should be enabled.
     *
     * @param keepAlive {@code true} if keep-alive should be enabled, or {@code false} otherwise.
     * @return This object.
     * @see Socket#setKeepAlive(boolean)
     */
    public FTPEnvironment withKeepAlive(boolean keepAlive) {
        put(KEEP_ALIVE, keepAlive);
        return this;
    }

    /**
     * Stores whether or not {@code SO_LINGER} should be enabled, and if so, the linger time.
     *
     * @param on     {@code true} if {@code SO_LINGER} should be enabled, or {@code false} otherwise.
     * @param linger The linger time in seconds, if {@code on} is {@code true}.
     * @return This object.
     * @see Socket#setSoLinger(boolean, int)
     */
    public FTPEnvironment withSoLinger(boolean on, int linger) {
        put(SO_LINGER_ON, on);
        put(SO_LINGER_VALUE, linger);
        return this;
    }

    /**
     * Stores the socket factory to use.
     *
     * @param factory The socket factory to use.
     * @return This object.
     */
    public FTPEnvironment withSocketFactory(SocketFactory factory) {
        put(SOCKET_FACTORY, factory);
        return this;
    }

    /**
     * Stores the server socket factory to use.
     *
     * @param factory The server socket factory to use.
     * @return This object.
     */
    public FTPEnvironment withServerSocketFactory(ServerSocketFactory factory) {
        put(SERVER_SOCKET_FACTORY, factory);
        return this;
    }

    /**
     * Stores the connection timeout to use.
     *
     * @param timeout The connection timeout in milliseconds.
     * @return This object.
     * @see Socket#connect(SocketAddress, int)
     */
    public FTPEnvironment withConnectTimeout(int timeout) {
        put(CONNECT_TIMEOUT, timeout);
        return this;
    }

    /**
     * Stores the proxy to use.
     *
     * @param proxy The proxy to use.
     * @return This object.
     */
    public FTPEnvironment withProxy(Proxy proxy) {
        put(PROXY, proxy);
        return this;
    }

    /**
     * Stores the charset to use.
     *
     * @param charset The charset to use.
     * @return This object.
     */
    public FTPEnvironment withCharset(Charset charset) {
        put(CHARSET, charset);
        return this;
    }

    // FTP

    /**
     * Stores the character encoding to be used by the FTP control connection.
     * Some FTP servers require that commands be issued in a non-ASCII encoding like UTF-8 so that filenames with multi-byte character
     * representations (e.g, Big 8) can be specified.
     *
     * @param encoding The character encoding to use.
     * @return This object.
     */
    public FTPEnvironment withControlEncoding(String encoding) {
        put(CONTROL_ENCODING, encoding);
        return this;
    }

    /**
     * Stores whether or not strict multiline parsing should be enabled, as per RFC 959, section 4.2.
     *
     * @param strictMultilineParsing {@code true} to enable strict multiline parsing, or {@code false} to disable it.
     * @return This object.
     */
    public FTPEnvironment withStrictlyMultilineParsing(boolean strictMultilineParsing) {
        put(STRICT_MULTILINE_PARSING, strictMultilineParsing);
        return this;
    }

    // FTPClient

    /**
     * Stores the timeout in milliseconds to use when reading from data connections.
     *
     * @param timeout The timeout in milliseconds that is used when opening data connection sockets.
     * @return This object.
     */
    public FTPEnvironment withDataTimeout(int timeout) {
        put(DATA_TIMEOUT, timeout);
        return this;
    }

    /**
     * Stores the factory used for parser creation.
     *
     * @param parserFactory The factory object used to create {@link FTPFileEntryParser}s
     * @return This object.
     */
    public FTPEnvironment withParserFactory(FTPFileEntryParserFactory parserFactory) {
        put(PARSER_FACTORY, parserFactory);
        return this;
    }

    /**
     * Stores whether or not verification that the remote host taking part of a data connection is the same as the host to which the control
     * connection is attached should be enabled.
     *
     * @param enabled {@code true} to enable verification, or {@code false} to disable verification.
     * @return This object.
     */
    public FTPEnvironment withRemoteVerificationEnabled(boolean enabled) {
        put(REMOTE_VERIFICATION_ENABLED, enabled);
        return this;
    }

    /**
     * Stores the default directory to use.
     * If it exists, this will be the directory that relative paths are resolved to.
     *
     * @param pathname The default directory to use.
     * @return This object.
     */
    public FTPEnvironment withDefaultDirectory(String pathname) {
        put(DEFAULT_DIR, pathname);
        return this;
    }

    /**
     * Stores the connection mode to use.
     * If the connection mode is not set, it will default to {@link ConnectionMode#ACTIVE}.
     *
     * @param connectionMode The connection mode to use.
     * @return This object.
     */
    public FTPEnvironment withConnectionMode(ConnectionMode connectionMode) {
        put(CONNECTION_MODE, connectionMode);
        return this;
    }

    /**
     * Stores the client side port range in active mode.
     *
     * @param minPort The lowest available port (inclusive).
     * @param maxPort The highest available port (inclusive).
     * @return This object.
     */
    public FTPEnvironment withActivePortRange(int minPort, int maxPort) {
        put(ACTIVE_PORT_RANGE_MIN, minPort);
        put(ACTIVE_PORT_RANGE_MAX, maxPort);
        return this;
    }

    /**
     * Stores the external IP address in active mode. Useful when there are multiple network cards.
     *
     * @param ipAddress The external IP address of this machine.
     * @return This object.
     */
    public FTPEnvironment withActiveExternalIPAddress(String ipAddress) {
        put(ACTIVE_EXTERNAL_IP_ADDRESS, ipAddress);
        return this;
    }

    /**
     * Stores the local IP address to use in passive mode. Useful when there are multiple network cards.
     *
     * @param ipAddress The local IP address of this machine.
     * @return This object.
     */
    public FTPEnvironment withPassiveLocalIPAddress(String ipAddress) {
        put(PASSIVE_LOCAL_IP_ADDRESS, ipAddress);
        return this;
    }

    /**
     * Stores the external IP address to report in EPRT/PORT commands in active mode. Useful when there are multiple network cards.
     *
     * @param ipAddress The external IP address of this machine.
     * @return This object.
     */
    public FTPEnvironment withReportActiveExternalIPAddress(String ipAddress) {
        put(REPORT_ACTIVE_EXTERNAL_IP_ADDRESS, ipAddress);
        return this;
    }

    /**
     * Stores the buffer size to use.
     *
     * @param bufferSize The buffer size to use.
     * @return This object.
     */
    public FTPEnvironment withBufferSize(int bufferSize) {
        put(BUFFER_SIZE, bufferSize);
        return this;
    }

    /**
     * Stores the value to use for the data socket {@code SO_SNDBUF} option.
     *
     * @param bufferSizr The size of the buffer.
     * @return This object.
     */
    public FTPEnvironment withSendDataSocketBufferSize(int bufferSizr) {
        put(SEND_DATA_SOCKET_BUFFER_SIZE, bufferSizr);
        return this;
    }

    /**
     * Stores the value to use for the data socket {@code SO_RCVBUF} option.
     *
     * @param bufferSize The size of the buffer.
     * @return This object.
     */
    public FTPEnvironment withReceiveDataSocketBufferSize(int bufferSize) {
        put(RECEIVE_DATA_SOCKET_BUFFER_SIZE, bufferSize);
        return this;
    }

    /**
     * Stores the FTP client config to use.
     *
     * @param clientConfig The client config to use.
     * @return This object.
     */
    public FTPEnvironment withClientConfig(FTPClientConfig clientConfig) {
        put(CLIENT_CONFIG, clientConfig);
        return this;
    }

    /**
     * Stores whether or not to use EPSV with IPv4. Might be worth enabling in some circumstances.
     * For example, when using IPv4 with NAT it may work with some rare configurations.
     * E.g. if FTP server has a static PASV address (external network) and the client is coming from another internal network.
     * In that case the data connection after PASV command would fail, while EPSV would make the client succeed by taking just the port.
     *
     * @param selected The flag to use.
     * @return This object.
     */
    public FTPEnvironment withUseEPSVwithIPv4(boolean selected) {
        put(USE_EPSV_WITH_IPV4, selected);
        return this;
    }

    /**
     * Stores the time to wait between sending control connection keep-alive messages when processing file upload or download.
     *
     * @param timeout The keep-alive timeout to use, in milliseconds.
     * @return This object.
     */
    public FTPEnvironment withControlKeepAliveTimeout(long timeout) {
        put(CONTROL_KEEP_ALIVE_TIMEOUT, timeout);
        return this;
    }

    /**
     * Stores how long to wait for control keep-alive message replies.
     *
     * @param timeout The keep-alive reply timeout to use, in milliseconds.
     * @return This object.
     */
    public FTPEnvironment withControlKeepAliveReplyTimeout(int timeout) {
        put(CONTROL_KEEP_ALIVE_REPLY_TIMEOUT, timeout);
        return this;
    }

    /**
     * Stores the workaround strategy to replace the PASV mode reply addresses.
     * This gets around the problem that some NAT boxes may change the reply.
     * The default implementation is {@link FTPClient.NatServerResolverImpl}, i.e. site-local replies are replaced.
     *
     * @param resolver The workaround strategy to replace internal IP's in passive mode, or {@code null} to disable the workaround
     *                 (i.e. use PASV mode reply address.)
     * @return This object.
     */
    public FTPEnvironment withPassiveNatWorkaroundStrategy(FTPClient.HostnameResolver resolver) {
        put(PASSIVE_NAT_WORKAROUND_STRATEGY, resolver);
        return this;
    }

    /**
     * Stores whether or not automatic server encoding detection should be enabled.
     * Note that only UTF-8 is supported.
     *
     * @param autodetect {@code true} to enable automatic server encoding detection, or {@code false} to disable it.
     * @return This object.
     */
    public FTPEnvironment withAutodetectEncoding(boolean autodetect) {
        put(AUTODETECT_ENCODING, autodetect);
        return this;
    }

    // FTP file system support

    /**
     * Stores the number of client connections to use. This value influences the number of concurrent threads that can access an FTP file system.
     *
     * @param count The number of client connection to use.
     * @return This object.
     */
    public FTPEnvironment withClientConnectionCount(int count) {
        put(CLIENT_CONNECTION_COUNT, count);
        return this;
    }

    /**
     * Stores the file system exception factory to use.
     *
     * @param factory The file system exception factory to use.
     * @return This object.
     */
    public FTPEnvironment withFileSystemExceptionFactory(FileSystemExceptionFactory factory) {
        put(FILE_SYSTEM_EXCEPTION_FACTORY, factory);
        return this;
    }

    String getUsername() {
        return FileSystemProviderSupport.getValue(this, USERNAME, String.class, null);
    }

    FileType getDefaultFileType() {
        // explicitly set in initializePostConnect
        return FileType.binary();
    }

    FileStructure getDefaultFileStructure() {
        // as specified by FTPClient
        return FileStructure.FILE;
    }

    FileTransferMode getDefaultFileTransferMode() {
        // as specified by FTPClient
        return FileTransferMode.STREAM;
    }

    int getClientConnectionCount() {
        int count = FileSystemProviderSupport.getIntValue(this, CLIENT_CONNECTION_COUNT, DEFAULT_CLIENT_CONNECTION_COUNT);
        return Math.max(1, count);
    }

    FileSystemExceptionFactory getExceptionFactory() {
        return FileSystemProviderSupport.getValue(this, FILE_SYSTEM_EXCEPTION_FACTORY, FileSystemExceptionFactory.class,
                DefaultFileSystemExceptionFactory.INSTANCE);
    }

    FTPClient createClient(String hostname, int port) throws IOException {
        FTPClient client = new FTPClient();
        initializePreConnect(client);
        connect(client, hostname, port);
        initializePostConnect(client);
        verifyConnection(client);
        return client;
    }

    void initializePreConnect(FTPClient client) throws IOException {
        client.setListHiddenFiles(true);

        if (containsKey(SEND_BUFFER_SIZE)) {
            int size = FileSystemProviderSupport.getIntValue(this, SEND_BUFFER_SIZE);
            client.setSendBufferSize(size);
        }
        if (containsKey(RECEIVE_BUFFER_SIZE)) {
            int size = FileSystemProviderSupport.getIntValue(this, RECEIVE_BUFFER_SIZE);
            client.setReceiveBufferSize(size);
        }

        if (containsKey(SOCKET_FACTORY)) {
            SocketFactory factory = FileSystemProviderSupport.getValue(this, SOCKET_FACTORY, SocketFactory.class, null);
            client.setSocketFactory(factory);
        }
        if (containsKey(SERVER_SOCKET_FACTORY)) {
            ServerSocketFactory factory = FileSystemProviderSupport.getValue(this, SERVER_SOCKET_FACTORY, ServerSocketFactory.class, null);
            client.setServerSocketFactory(factory);
        }

        if (containsKey(CONNECT_TIMEOUT)) {
            int connectTimeout = FileSystemProviderSupport.getIntValue(this, CONNECT_TIMEOUT);
            client.setConnectTimeout(connectTimeout);
        }

        if (containsKey(PROXY)) {
            Proxy proxy = FileSystemProviderSupport.getValue(this, PROXY, Proxy.class, null);
            client.setProxy(proxy);
        }
        if (containsKey(CHARSET)) {
            Charset charset = FileSystemProviderSupport.getValue(this, CHARSET, Charset.class, null);
            client.setCharset(charset);
        }
        if (containsKey(CONTROL_ENCODING)) {
            String controlEncoding = FileSystemProviderSupport.getValue(this, CONTROL_ENCODING, String.class, null);
            client.setControlEncoding(controlEncoding);
        }

        if (containsKey(STRICT_MULTILINE_PARSING)) {
            boolean strictMultilineParsing = FileSystemProviderSupport.getBooleanValue(this, STRICT_MULTILINE_PARSING);
            client.setStrictMultilineParsing(strictMultilineParsing);
        }
        if (containsKey(DATA_TIMEOUT)) {
            int timeout = FileSystemProviderSupport.getIntValue(this, DATA_TIMEOUT);
            client.setDataTimeout(timeout);
        }

        if (containsKey(PARSER_FACTORY)) {
            FTPFileEntryParserFactory parserFactory = FileSystemProviderSupport.getValue(this, PARSER_FACTORY, FTPFileEntryParserFactory.class, null);
            client.setParserFactory(parserFactory);
        }

        if (containsKey(REMOTE_VERIFICATION_ENABLED)) {
            boolean enable = FileSystemProviderSupport.getBooleanValue(this, REMOTE_VERIFICATION_ENABLED);
            client.setRemoteVerificationEnabled(enable);
        }

        FileSystemProviderSupport.getValue(this, CONNECTION_MODE, ConnectionMode.class, ConnectionMode.ACTIVE).apply(client);

        if (containsKey(ACTIVE_PORT_RANGE_MIN) && containsKey(ACTIVE_PORT_RANGE_MAX)) {
            int minPort = FileSystemProviderSupport.getIntValue(this, ACTIVE_PORT_RANGE_MIN);
            int maxPort = FileSystemProviderSupport.getIntValue(this, ACTIVE_PORT_RANGE_MAX);
            client.setActivePortRange(minPort, maxPort);
        }

        if (containsKey(ACTIVE_EXTERNAL_IP_ADDRESS)) {
            String ipAddress = FileSystemProviderSupport.getValue(this, ACTIVE_EXTERNAL_IP_ADDRESS, String.class, null);
            client.setActiveExternalIPAddress(ipAddress);
        }
        if (containsKey(PASSIVE_LOCAL_IP_ADDRESS)) {
            String ipAddress = FileSystemProviderSupport.getValue(this, PASSIVE_LOCAL_IP_ADDRESS, String.class, null);
            client.setPassiveLocalIPAddress(ipAddress);
        }
        if (containsKey(REPORT_ACTIVE_EXTERNAL_IP_ADDRESS)) {
            String ipAddress = FileSystemProviderSupport.getValue(this, REPORT_ACTIVE_EXTERNAL_IP_ADDRESS, String.class, null);
            client.setReportActiveExternalIPAddress(ipAddress);
        }

        if (containsKey(BUFFER_SIZE)) {
            int bufSize = FileSystemProviderSupport.getIntValue(this, BUFFER_SIZE);
            client.setBufferSize(bufSize);
        }
        if (containsKey(SEND_DATA_SOCKET_BUFFER_SIZE)) {
            int bufSize = FileSystemProviderSupport.getIntValue(this, SEND_DATA_SOCKET_BUFFER_SIZE);
            client.setSendDataSocketBufferSize(bufSize);
        }
        if (containsKey(RECEIVE_DATA_SOCKET_BUFFER_SIZE)) {
            int bufSize = FileSystemProviderSupport.getIntValue(this, RECEIVE_DATA_SOCKET_BUFFER_SIZE);
            client.setReceieveDataSocketBufferSize(bufSize);
        }

        if (containsKey(CLIENT_CONFIG)) {
            FTPClientConfig clientConfig = FileSystemProviderSupport.getValue(this, CLIENT_CONFIG, FTPClientConfig.class, null);
            if (clientConfig != null) {
                clientConfig = new FTPClientConfig(clientConfig);
            }
            client.configure(clientConfig);
        }

        if (containsKey(PASSIVE_NAT_WORKAROUND_STRATEGY)) {
            FTPClient.HostnameResolver resolver = FileSystemProviderSupport.getValue(this, PASSIVE_NAT_WORKAROUND_STRATEGY, FTPClient.HostnameResolver.class, null);
            client.setPassiveNatWorkaroundStrategy(resolver);
        }

        if (containsKey(USE_EPSV_WITH_IPV4)) {
            boolean selected = FileSystemProviderSupport.getBooleanValue(this, USE_EPSV_WITH_IPV4);
            client.setUseEPSVwithIPv4(selected);
        }
        if (containsKey(CONTROL_KEEP_ALIVE_TIMEOUT)) {
            long controlIdle = FileSystemProviderSupport.getLongValue(this, CONTROL_KEEP_ALIVE_TIMEOUT);
            // the value is stored as ms, but the method expects seconds
            controlIdle = TimeUnit.MILLISECONDS.toSeconds(controlIdle);
            client.setControlKeepAliveTimeout(controlIdle);
        }
        if (containsKey(CONTROL_KEEP_ALIVE_REPLY_TIMEOUT)) {
            int timeout = FileSystemProviderSupport.getIntValue(this, CONTROL_KEEP_ALIVE_REPLY_TIMEOUT);
            client.setControlKeepAliveReplyTimeout(timeout);
        }
        if (containsKey(AUTODETECT_ENCODING)) {
            boolean autodetect = FileSystemProviderSupport.getBooleanValue(this, AUTODETECT_ENCODING);
            client.setAutodetectUTF8(autodetect);
        }
    }

    void connect(FTPClient client, String hostname, int port) throws IOException {

        if (port == -1) {
            port = client.getDefaultPort();
        }

        InetAddress localAddr = FileSystemProviderSupport.getValue(this, LOCAL_ADDR, InetAddress.class, null);
        if (localAddr != null) {
            int localPort = FileSystemProviderSupport.getIntValue(this, LOCAL_PORT);
            client.connect(hostname, port, localAddr, localPort);
        } else {
            client.connect(hostname, port);
        }

        String username = getUsername();
        char[] passwordChars = FileSystemProviderSupport.getValue(this, PASSWORD, char[].class, null);
        String password = passwordChars != null ? new String(passwordChars) : null;
        String account = FileSystemProviderSupport.getValue(this, ACCOUNT, String.class, null);
        if (account != null) {
            if (!client.login(username, password, account)) {
                throw new FTPFileSystemException(client.getReplyCode(), client.getReplyString());
            }
        } else if (username != null || password != null) {
            if (!client.login(username, password)) {
                throw new FTPFileSystemException(client.getReplyCode(), client.getReplyString());
            }
        }
        // else no account or username/password - don't log in
    }

    void initializePostConnect(FTPClient client) throws IOException {
        if (containsKey(SO_TIMEOUT)) {
            int timeout = FileSystemProviderSupport.getIntValue(this, SO_TIMEOUT);
            client.setSoTimeout(timeout);
        }
        if (containsKey(TCP_NO_DELAY)) {
            boolean on = FileSystemProviderSupport.getBooleanValue(this, TCP_NO_DELAY);
            client.setTcpNoDelay(on);
        }
        if (containsKey(KEEP_ALIVE)) {
            boolean keepAlive = FileSystemProviderSupport.getBooleanValue(this, KEEP_ALIVE);
            client.setKeepAlive(keepAlive);
        }
        if (containsKey(SO_LINGER_ON) && containsKey(SO_LINGER_VALUE)) {
            boolean on = FileSystemProviderSupport.getBooleanValue(this, SO_LINGER_ON);
            int val = FileSystemProviderSupport.getIntValue(this, SO_LINGER_VALUE);
            client.setSoLinger(on, val);
        }

        // default to binary
        client.setFileType(FTP.BINARY_FILE_TYPE);

        String defaultDir = FileSystemProviderSupport.getValue(this, DEFAULT_DIR, String.class, null);
        if (defaultDir != null && !client.changeWorkingDirectory(defaultDir)) {
            throw getExceptionFactory().createChangeWorkingDirectoryException(defaultDir, client.getReplyCode(), client.getReplyString());
        }
    }

    void verifyConnection(FTPClient client) throws IOException {
        if (client.printWorkingDirectory() == null) {
            throw new FTPFileSystemException(client.getReplyCode(), client.getReplyString());
        }
    }

    // Map / Object

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public FTPEnvironment clone() {
        try {
            FTPEnvironment clone = (FTPEnvironment) super.clone();
            clone.map = new HashMap<>(map);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
