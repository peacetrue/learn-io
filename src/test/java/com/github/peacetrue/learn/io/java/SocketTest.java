package com.github.peacetrue.learn.io.java;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.awaitility.Awaitility;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.OS;

import javax.annotation.Nullable;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.peacetrue.test.ProcessBuilderUtils.*;

/**
 * @author peace
 **/
@Slf4j
class SocketTest {

    /** 环回网卡名称，Mac 和 CentOS 有区别 */
    private static String lo;
    /** 一个未被使用的随机端口，服务端会运行在此端口上 */
    private int serverPort = 10000;
    private int clientPort;

    public SocketTest() {
        setClientPort();
    }

    /**
     * 客户端端口 = 服务端端口 + 1。
     */
    void setClientPort() {
        clientPort = serverPort + 1;
    }

    @BeforeAll
    @SneakyThrows
    static void beforeAll() {
        prepareLoopback();
    }

    @BeforeEach
    void setUp() {
        prepareServerPort();
    }

    @SneakyThrows
    private static void prepareLoopback() {
        // 8.lo0 [Up, Running, Loopback]
        Process process = Runtime.getRuntime().exec(sh("tcpdump -D | grep Loopback | awk '{print $1}' | awk -F '[.:]' '{print $NF}'"));
        Assertions.assertEquals(0, process.waitFor());
        lo = StringUtils.strip(IOUtils.toString(process.getInputStream()));
        log.debug("lo: {}", lo);
    }

    @SneakyThrows
    private void prepareServerPort() {
        // tcp    0      0 127.0.0.1:25    0.0.0.0:*       LISTEN
        Process process = Runtime.getRuntime().exec(sh("netstat -nat | grep -E 'tcp' | awk '{print $4}' | awk -F '[.:]' '{print $NF}'"));
        Assertions.assertEquals(0, process.waitFor());
        String portsString = IOUtils.toString(process.getInputStream());
        log.debug("portsString:\n{}", portsString);
        Set<Integer> ports = Arrays.stream(portsString.split("\n"))
                .filter(NumberUtils::isNumber).map(Integer::parseInt)
                .collect(Collectors.toSet());
        while (ports.contains(serverPort) || ports.contains(clientPort)) {
            serverPort += 1;
            setClientPort();
        }
        log.debug("port: {}", serverPort);
    }


    /** 演示客户端服务端基本交互流程 */
    @Test
    @SneakyThrows
    void basic() {
        Assertions.assertNotNull(lo);

        Process process = tcpdump(20);

        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress("127.0.0.1", serverPort));
        netstat("server.bind");

        Socket client = new Socket();
        client.bind(new InetSocketAddress("127.0.0.1", clientPort));
        netstat("client.bind");
        client.connect(server.getLocalSocketAddress());
        netstat("client.connect");

        String request = "----";
        byte[] bytes = request.getBytes(StandardCharsets.UTF_8);
        client.getOutputStream().write(bytes);
        netstat("client.write");

        Socket acceptSocket = server.accept();
        Assertions.assertNotEquals(-1, acceptSocket.getInputStream().read(bytes));
        netstat("server.read");
        acceptSocket.getOutputStream().write(bytes);
        netstat("server.write");

        Assertions.assertNotEquals(-1, client.getInputStream().read(bytes));
        netstat("client.read");
        Assertions.assertEquals(request, new String(bytes));

        acceptSocket.close();
        netstat("acceptSocket.close");
        client.close();

        Awaitility.await().until(() -> {
            getBacklogClient(server);
            return !process.isAlive();
        });
    }

    @Test
    @SneakyThrows
    void ipv46() {
        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress(serverPort));
        netstat("server.bind");
        new Socket("localhost", serverPort);
        netstat("client.connect");
        new Socket("::1", serverPort);
        netstat("client.connect");
        server.close();

        server = new ServerSocket();
        server.bind(new InetSocketAddress("localhost", serverPort));
        netstat("server.bind");
        new Socket("localhost", serverPort);
        netstat("client.connect");
        Assertions.assertThrows(ConnectException.class, () -> new Socket("::1", serverPort), "Connection refused");
        server.close();

        server = new ServerSocket();
        server.bind(new InetSocketAddress("::1", serverPort));
        netstat("server.bind");
        Assertions.assertThrows(ConnectException.class, () -> new Socket("localhost", serverPort), "Connection refused");
        new Socket("::1", serverPort);
        netstat("client.connect");
        server.close();
    }

    @Test
    @SneakyThrows
    void addressAlreadyInUse() {
        // you can create a port listener using Netcat .
        // nc -l localhost 10000
        // nc -v localhost 10000
        new ServerSocket(serverPort);
        Assertions.assertThrows(BindException.class, () -> new ServerSocket(serverPort), "Address already in use");
        Assertions.assertThrows(BindException.class, () -> new ServerSocket().bind(new InetSocketAddress(serverPort)), "Address already in use");
        netstat("servers.bind");
    }

    @Test
    void connectionRefused() {
        ConnectException connectException = Assertions.assertThrows(ConnectException.class, () -> new Socket("localhost", serverPort));
        log.error("connectionRefused", connectException);
    }

    @Test
    @SneakyThrows
    void analyseConnectionRefused() {
        Assertions.assertNotEquals(0, execPipe("netstat -nat | grep -E 'tcp[^6].*" + serverPort + "'").waitFor());

        Process process = execPipe(sudoPipe("tcpdump -n -i " + lo + " -c 5 tcp and port " + serverPort));
        Thread thread = new Thread(Unchecked.runnable(() -> Assertions.assertEquals(0, process.waitFor())));
        thread.start();
        while (process.isAlive())
            Assertions.assertThrows(ConnectException.class, () -> new Socket("localhost", serverPort));
        thread.join();
    }

    /** 测试不指定 host 和指定 host 的区别。 */
    @Test
    @SneakyThrows
    void host() {
        List<String> ips = ips();
        log.info("ips: {}", ips);

        // 不指定 host 都能连
        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress(serverPort));
        netstat("server.bind");
        ips.forEach(ip -> Assertions.assertDoesNotThrow(() -> new Socket(ip, serverPort)));
        netstat("clients.connected");
        server.close();

        // 指定 host 为 localhost，只有 127.0.0.1 能连
        new ServerSocket().bind(new InetSocketAddress("localhost", serverPort));
        netstat("server.bind");
        IntStream.range(0, ips.size()).forEach(i -> {
            String ip = ips.get(i);
            log.info("ip: {}", ip);
            if (i == 0) Assertions.assertDoesNotThrow(() -> new Socket(ip, serverPort));
            else Assertions.assertThrows(ConnectException.class, () -> new Socket(ip, serverPort));
        });
        netstat("clients.connected");
    }

    @SneakyThrows
    private List<String> ips() {
        Process process = Runtime.getRuntime().exec(sh("ifconfig | grep -E 'inet[^6]' | awk '{print $2}'"));
        Assertions.assertEquals(0, process.waitFor());
        String ipsString = IOUtils.toString(process.getInputStream());
        return Stream.of(ipsString.split("\n")).sorted()
                .filter(ip -> ip.startsWith("127") || ip.startsWith("192.168"))
                .collect(Collectors.toList());
    }

    @Test
    @SneakyThrows
    void backlog() {
        Process process = tcpdump(20);

        int backlog = 2, limit = OS.MAC.isCurrentOs() ? backlog : backlog + 1;
        ServerSocket server = new ServerSocket(serverPort, backlog, InetAddress.getByName("127.0.0.1"));
        IntStream.range(0, limit).forEach(i -> Assertions.assertDoesNotThrow(() -> getBacklogClient(server)));
        netstat("clients.connected");

        // sysctl net.ipv4.tcp_syn_retries
        // sysctl -w net.ipv4.tcp_syn_retries = 6

        Assertions.assertThrows(SocketTimeoutException.class, () -> getBacklogClient(server), "connect timed out");

        server.accept();
        Assertions.assertDoesNotThrow(() -> getBacklogClient(server));

        Awaitility.await().until(() -> {
            server.accept();
            getBacklogClient(server);
            return !process.isAlive();
        });
    }

    @SneakyThrows
    private Socket getBacklogClient(ServerSocket server) {
        Socket client = new Socket();
        client.bind(new InetSocketAddress("127.0.0.1", clientPort++));
        client.connect(server.getLocalSocketAddress(), 1_000);
        return client;
    }

    /**
     * 测试 SO_REUSEADDR 的作用。
     *
     * @see <a href="https://stackoverflow.com/questions/51998042/macos-so-reuseaddr-so-reuseport-not-consistent-with-linux">MacOS SO_REUSEADDR 无效</a>
     */
    @Test
    @SneakyThrows
    void setReuseAddress() {
        ServerSocket server = new ServerSocket();
        server.setReuseAddress(false);
        server.bind(new InetSocketAddress("127.0.0.1", serverPort));
        netstat("server.bind");

        Socket client = getReuseAddressClient(server, false);
        netstat("client.connected");
        // 从客户端关闭，客户端进入 TIME_WAIT
        client.close();
        netstat("client.closed");
        server.accept().close();
        netstat("server.closed");
        Assertions.assertThrows(BindException.class, () -> getReuseAddressClient(server, false), "Address already in use");

        // 之前的客户端被占用，使用一个新客户端
        clientPort++;
        client = getReuseAddressClient(server, false);
        netstat("client.connected");
        // 从客户端关闭，客户端进入 TIME_WAIT
        client.close();
        netstat("client.closed");
        server.accept().close();
        netstat("server.closed");
        client = OS.MAC.isCurrentOs()
                ? awaitAvailableReuseAddressClient(server)
                : Assertions.assertDoesNotThrow(() -> getReuseAddressClient(server, true));
        netstat("client.connected");

        // 从服务端关闭，服务端进入 TIME_WAIT
        server.accept().close();
        netstat("server.closed");
        client.close();
        netstat("client.closed");
        Assertions.assertThrows(SocketTimeoutException.class, () -> getReuseAddressClient(server, false), "Connect timed out");
    }

    /**
     * 等到客户端 TIME_WAIT 耗尽，可以获取到连接。
     *
     * @param server 服务端
     * @return 客户端
     */
    private Socket awaitAvailableReuseAddressClient(ServerSocket server) {
        return Awaitility.await().forever()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> getAvailableReuseAddressClient(server), Objects::nonNull);
    }

    @Nullable
    private Socket getAvailableReuseAddressClient(ServerSocket server) {
        try {
            netstat("TIME_WAIT Exhausted?");
            return getReuseAddressClient(server, true);
        } catch (Exception e) {
            return null;
        }
    }

    @SneakyThrows
    private Socket getReuseAddressClient(ServerSocket server, boolean reuseAddress) {
        Socket socket = new Socket();
        socket.setReuseAddress(reuseAddress);
        socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), clientPort));
        socket.connect(server.getLocalSocketAddress(), 1_000);
        return socket;
    }

    @SneakyThrows
    private void netstat(String title) {
        Process process = Runtime.getRuntime().exec(sh(
                String.format("netstat -nat%s | grep '%s'", OS.MAC.isCurrentOs() ? "" : "p", serverPort)
        ));
        Assertions.assertEquals(0, process.waitFor());
        log.info("{}\n{}", title, IOUtils.toString(process.getInputStream()));
    }

    private Process tcpdump(int count) {
        return tcpdump("tcpdump.listening:", count);
    }

    @SneakyThrows
    private Process tcpdump(String title, int count) {
        Process process = Runtime.getRuntime().exec("tcpdump -n -i " + lo + " -c " + count + " tcp and port " + serverPort);
        new Thread(Unchecked.runnable(() -> {
            Assertions.assertEquals(0, process.waitFor());
            log.info("{}\n{}", title, IOUtils.toString(process.getInputStream()));
        })).start();
        Awaitility.await().until(process::isAlive);
        return process;
    }


}
