package com.github.peacetrue.learn.io.java;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.awaitility.Awaitility;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.*;
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

    /** 一个不存在的随机端口 */
    private static int port = 10000;
    /** 环回网卡名称 */
    private static String lo;

    @BeforeAll
    @SneakyThrows
    static void beforeAll() {
        // tcp    0      0 127.0.0.1:25    0.0.0.0:*       LISTEN
        Process process = Runtime.getRuntime().exec(sh("netstat -nat | grep -E 'tcp.*LISTEN' | awk '{print $4}' | awk -F '.' '{print $NF}'"));
        Assertions.assertEquals(0, process.waitFor());
        String portsString = IOUtils.toString(process.getInputStream());
        Set<Integer> ports = Arrays.stream(portsString.split("\n")).map(Integer::parseInt).collect(Collectors.toSet());
        int min = 10_000, max = (int) Math.pow(2, 16) - min;
        while (ports.contains(port)) port = RandomUtils.nextInt(max) + min;
        log.debug("port: {}", port);

        // 8.lo0 [Up, Running, Loopback]
        process = Runtime.getRuntime().exec(sh("tcpdump -D | grep Loopback | awk '{print $1}' | awk -F '.' '{print $NF}'"));
        Assertions.assertEquals(0, process.waitFor());
        lo = StringUtils.strip(IOUtils.toString(process.getInputStream()));
        log.debug("lo: {}", lo);
    }

    @Test
    @SneakyThrows
    void Address_already_in_use() {
        // you can create a port listener using Netcat .
        // nc -l localhost 10000
        // nc -v localhost 10000
        new ServerSocket(port);
        BindException exception = Assertions.assertThrows(BindException.class, () -> new ServerSocket(port), "Address already in use");
        log.error("new ServerSocket(port)", exception);
        exception = Assertions.assertThrows(BindException.class, () -> new ServerSocket().bind(new InetSocketAddress(port)), "Address already in use");
        log.error("new ServerSocket().bind", exception);
        Assertions.assertEquals(0, execPipe("netstat -nat | grep -E 'tcp.*" + port + "'").waitFor());
    }

    @Test
    void connectionRefused() {
        ConnectException connectException = Assertions.assertThrows(ConnectException.class, () -> new Socket("localhost", port));
        log.error("connectionRefused", connectException);
    }

    @Test
    @SneakyThrows
    void analyseConnectionRefused() {
        Assertions.assertNotEquals(0, execPipe("netstat -nat | grep -E 'tcp[^6].*" + port + "'").waitFor());

        Process process = execPipe(sudoPipe("tcpdump -n -i " + lo + " -c 5 tcp and port " + port));
        Thread thread = new Thread(Unchecked.runnable(() -> Assertions.assertEquals(0, process.waitFor())));
        thread.start();
        while (process.isAlive()) Assertions.assertThrows(ConnectException.class, () -> new Socket("localhost", port));
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
        serverSocket.bind(new InetSocketAddress(port));
        ips.forEach(ip -> Assertions.assertDoesNotThrow(() -> new Socket(ip, port)));
        execPipe("netstat -nat | grep -E 'tcp.*" + port + "'").waitFor();
        serverSocket.close();

        new ServerSocket().bind(new InetSocketAddress("localhost", port));
        IntStream.range(1, ips.size()).forEach(i -> Assertions.assertThrows(ConnectException.class, () -> new Socket(ips.get(i), port)));
        execPipe("netstat -nat | grep -E 'tcp.*" + port + "'").waitFor();
    }


    @Test
    @SneakyThrows
    void backlog() {
        int backlog = 2;
        ServerSocket serverSocket = new ServerSocket(port, backlog);
        IntStream.range(0, backlog).boxed().forEach(i -> Assertions.assertDoesNotThrow(() -> new Socket("localhost", port)));
        execPipe("netstat -nat | grep -E '" + port + "'").waitFor(); // netstat -nat | grep 10000

        Process process = execPipe(sudoPipe("tcpdump -n -i " + lo + " -c 10 tcp and port " + port));
        Thread thread = new Thread(Unchecked.runnable(process::waitFor));
        thread.start();
        Awaitility.await().until(process::isAlive);
        // net.ipv4.tcp_syn_retries = 6
        // sysctl -w net.ipv4.tcp_syn_retries = 6
        // sysctl net.ipv4.tcp_syncookies
        // sysctl net.ipv4.tcp_syncookies=1
//        execPipe(sudoPipe("sysctl -a | grep tcp_syn_retries")).waitFor();
//        execPipe(sudoPipe("sysctl net.ipv4.tcp_syn_retries=1")).waitFor();
        new Thread(Unchecked.runnable(() -> execPipe("netstat -nat | grep -E '" + port + "'").waitFor()));
        Exception exception = Assertions.assertThrows(Exception.class, () -> new Socket("localhost", port), "Operation timed out");
        log.error("ConnectException", exception);

        serverSocket.accept();
        Assertions.assertDoesNotThrow(() -> new Socket("localhost", port));
    }


}
