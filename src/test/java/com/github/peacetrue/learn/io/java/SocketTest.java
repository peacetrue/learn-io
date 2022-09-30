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

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

    /**
     * 客户端端口 = 服务端端口 + 1。
     *
     * @return 客户端端口
     */
    int getClientPort() {
        return serverPort + 1;
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
//        log.debug("portsString:\n{}", portsString);
        Set<Integer> ports = Arrays.stream(portsString.split("\n")).filter(NumberUtils::isNumber).map(Integer::parseInt).collect(Collectors.toSet());
        while (ports.contains(serverPort)) serverPort += 10;
        log.debug("port: {}", serverPort);
    }


    /** 演示客户端服务端基本交互流程 */
    @Test
    @SneakyThrows
    void basic() {
        Assertions.assertNotNull(lo);

        Process process = tcpdump("tcpdump.listening:", 20);

        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), serverPort));
        netstat("server.bind:");

        Socket client = new Socket();
        client.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), getClientPort()));
        netstat("client.bind:");
        client.connect(server.getLocalSocketAddress());
        netstat("client.connect:");

        String request = "----";
        byte[] bytes = request.getBytes(StandardCharsets.UTF_8);
        client.getOutputStream().write(bytes);
        netstat("client.write:");

        Socket acceptSocket = server.accept();
        Assertions.assertNotEquals(-1, acceptSocket.getInputStream().read(bytes));
        netstat("server.read:");
        acceptSocket.getOutputStream().write(bytes);
        netstat("server.write:");

        Assertions.assertNotEquals(-1, client.getInputStream().read(bytes));
        netstat("client.read:");
        Assertions.assertEquals(request, new String(bytes));

        acceptSocket.close();
        netstat("acceptSocket.close:");
        client.close();

        Awaitility.await().until(() -> {
            new Socket().connect(server.getLocalSocketAddress(), 1_000);
            return !process.isAlive();
        });
    }

    @Test
    @SneakyThrows
    void addressAlreadyInUse() {
        // you can create a port listener using Netcat .
        // nc -l localhost 10000
        // nc -v localhost 10000
        new ServerSocket(serverPort);
        BindException exception = Assertions.assertThrows(BindException.class, () -> new ServerSocket(serverPort), "Address already in use");
        log.error("new ServerSocket(port)", exception);
        exception = Assertions.assertThrows(BindException.class, () -> new ServerSocket().bind(new InetSocketAddress(serverPort)), "Address already in use");
        log.error("new ServerSocket().bind", exception);
        Assertions.assertEquals(0, execPipe("netstat -nat | grep -E 'tcp.*" + serverPort + "'").waitFor());
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

    @Test
    @SneakyThrows
    void host() {
        Process process = Runtime.getRuntime().exec(sh("ifconfig | grep -E 'inet[^6]' | awk '{print $2}'"));
        String ipsString = IOUtils.toString(process.getInputStream());
        List<String> ips = Stream.of(ipsString.split("\n")).sorted().collect(Collectors.toList());
        log.info("ips: {}", ips);

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(serverPort));
        ips.forEach(ip -> Assertions.assertDoesNotThrow(() -> new Socket(ip, serverPort)));
        execPipe("netstat -nat | grep -E 'tcp.*" + serverPort + "'").waitFor();
        serverSocket.close();

        new ServerSocket().bind(new InetSocketAddress("localhost", serverPort));
        IntStream.range(1, ips.size()).forEach(i -> Assertions.assertThrows(ConnectException.class, () -> new Socket(ips.get(i), serverPort)));
        execPipe("netstat -nat | grep -E 'tcp.*" + serverPort + "'").waitFor();
    }


    @Test
    @SneakyThrows
    void backlog() {
        int backlog = 2;
        ServerSocket serverSocket = new ServerSocket(serverPort, backlog);
        IntStream.range(0, backlog).boxed().forEach(i ->
                Assertions.assertDoesNotThrow(() -> new Socket().connect(serverSocket.getLocalSocketAddress(), 1_000))
        );
        execPipe("netstat -nat | grep -E '" + serverPort + "'").waitFor();

        tcpdump(10);
        // net.ipv4.tcp_syn_retries = 6
        // sysctl -w net.ipv4.tcp_syn_retries = 6
        // sysctl net.ipv4.tcp_syncookies
        // sysctl net.ipv4.tcp_syncookies=1
//        execPipe(sudoPipe("sysctl -a | grep tcp_syn_retries")).waitFor();
//        execPipe(sudoPipe("sysctl net.ipv4.tcp_syn_retries=1")).waitFor();

        SocketTimeoutException exception = Assertions.assertThrows(
                SocketTimeoutException.class, () -> new Socket().connect(serverSocket.getLocalSocketAddress(), 1_000)
        );
        log.error("ConnectException", exception);

        serverSocket.accept();
        Assertions.assertDoesNotThrow(
                () -> new Socket().connect(serverSocket.getLocalSocketAddress(), 1_000)
        );
    }


    @Test
    @SneakyThrows
    void setReuseAddress() {
        ServerSocket server = new ServerSocket();
        server.setReuseAddress(true);
        server.bind(new InetSocketAddress(serverPort));
        netstat("server.bind");

        Socket client = getClient(server);
        netstat("client.connect");
        client.close();
        netstat("client.close");
        server.accept().close();
        netstat("server.close");

        Awaitility.await().untilAsserted(() -> {
            netstat("reconnect");
            Assertions.assertDoesNotThrow(() -> getClient(server));
        });
    }

    @SneakyThrows
    private Socket getClient(ServerSocket serverSocket) {
        Socket socket = new Socket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), getClientPort()));
        socket.connect(serverSocket.getLocalSocketAddress(), 1_000);
        return socket;
    }

    @SneakyThrows
    private void netstat(String title) {
        Process process = Runtime.getRuntime().exec(sh("netstat -nat | grep -E '" + serverPort + "'"));
        Assertions.assertEquals(0, process.waitFor());
        log.debug("{}\n{}", title, IOUtils.toString(process.getInputStream()));
    }

    private Process tcpdump(int count) {
        return tcpdump("", count);
    }

    @SneakyThrows
    private Process tcpdump(String title, int count) {
        Process process = Runtime.getRuntime().exec("tcpdump -n -i " + lo + " -c " + count + " tcp and port " + serverPort);
        new Thread(Unchecked.runnable(() -> {
            Assertions.assertEquals(0, process.waitFor());
            log.debug("{}\n{}", title, IOUtils.toString(process.getInputStream()));
        })).start();
        Awaitility.await().until(process::isAlive);
        return process;
    }


}
