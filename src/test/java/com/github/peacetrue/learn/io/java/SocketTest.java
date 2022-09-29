package com.github.peacetrue.learn.io.java;

import com.github.peacetrue.test.ProcessBuilderUtils;
import com.github.peacetrue.test.SourcePathUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.*;
import java.util.stream.IntStream;

import static com.github.peacetrue.test.ProcessBuilderUtils.execPipe;
import static com.github.peacetrue.test.ProcessBuilderUtils.sudoPipe;

/**
 * @author peace
 **/
@Slf4j
class SocketTest {

    @Test
    @SneakyThrows
    void netstat() {
        // -f address_family  inet, inet6, unix
        // -p protocol ip, tcp, icmp
        // Mac netstat
        Assertions.assertEquals(0, execPipe("netstat -nat | grep -E 'tcp.*LISTEN' | wc -l").waitFor());
    }

    @Test
    @SneakyThrows
    void Address_already_in_use() {
        // you can create a port listener using Netcat .
        // nc -l localhost 1000
        // nc -v localhost 1000
        int port = 1000;
        new ServerSocket(port);
        BindException exception = Assertions.assertThrows(BindException.class, () -> new ServerSocket(port), "Address already in use");
        log.error("BindException", exception);
        exception = Assertions.assertThrows(BindException.class, () -> new ServerSocket().bind(new InetSocketAddress(port)), "Address already in use");
        log.error("BindException", exception);
        Assertions.assertEquals(0, execPipe("netstat -nat | grep -E 'tcp.*LISTEN" + port + "'").waitFor());
    }

    @Test
    void connectionRefused() {
        Assertions.assertThrows(ConnectException.class, () -> new Socket("localhost", 1000)).printStackTrace();
    }

    @Test
    @SneakyThrows
    void analyseConnectionRefused() {
        int port = 1000;
        Assertions.assertNotEquals(0, execPipe("netstat -nat | grep -E 'tcp4[^6]" + port).waitFor());

        Thread thread = new Thread(Unchecked.runnable(() -> {
            File file = new File(SourcePathUtils.getTestResourceAbsolutePath("/pass.txt"));
            int exitValue = ProcessBuilderUtils.execSudo(file, ("tcpdump -n -i lo0 -c 5 tcp and port " + port).split(" ")).waitFor();
            Assertions.assertEquals(0, exitValue);
        }));
        thread.start();
        while (thread.isAlive()) Assertions.assertThrows(ConnectException.class, () -> new Socket("localhost", port));
        thread.join();
    }

    @Test
    void bindHostPermissionDenied() {
        int port = 1000;
        BindException exception = Assertions.assertThrows(
                BindException.class,
                () -> new ServerSocket().bind(new InetSocketAddress("localhost", port)),
                "Permission denied"
        );
        log.debug("BindException", exception);
        // Permission denied
        // java.net.BindException: Permission denied
    }

    @Test
    @SneakyThrows
    void bindHostByNCAll() {
        int port = 1000;
        Process process = execPipe(sudoPipe("nc -l " + port));
        Thread thread = new Thread(Unchecked.runnable(process::waitFor));
        thread.start();
        Awaitility.await().until(process::isAlive);
        Awaitility.await().untilAsserted(() -> new Socket("localhost", port));
        Assertions.assertDoesNotThrow(() -> new Socket("192.168.1.5", port));
        Assertions.assertEquals(0, execPipe("netstat -nat | grep -E '" + port + "'").waitFor());
        process.destroy();
    }

    @Test
    @SneakyThrows
    void bindHostByNCLocalhost() {
        int port = 1000;
        Process process = execPipe(sudoPipe("nc -l localhost " + port));
        Thread thread = new Thread(Unchecked.runnable(process::waitFor));
        thread.start();
        Awaitility.await().until(process::isAlive);
        Awaitility.await().untilAsserted(() -> new Socket("localhost", port));
        ConnectException exception = Assertions.assertThrows(ConnectException.class, () -> new Socket("192.168.1.5", port));
        log.debug("ConnectException", exception);
    }

    @Test
    @SneakyThrows
    void connection() {
        int port = 1000;
        new ServerSocket(port);
        new Socket("localhost", port);
        Assertions.assertEquals(0, execPipe("netstat -nat | grep -E '" + port + "'").waitFor());
    }

    @Test
    @SneakyThrows
    void backlog() {
        int port = 1000;
        int backlog = 2;
        ServerSocket serverSocket = new ServerSocket(port, backlog);
        IntStream.range(0, backlog).boxed().forEach(i -> Assertions.assertDoesNotThrow(() -> new Socket("localhost", port)));
        // netstat -nat | grep 1000
        Assertions.assertEquals(0, execPipe("netstat -nat | grep -E '" + port + "'").waitFor());

        Process process = execPipe(sudoPipe("tcpdump -n -i lo0 -c 10 tcp and port " + port));
        Thread thread = new Thread(Unchecked.runnable(() -> Assertions.assertEquals(0, process.waitFor())));
        thread.start();
        Awaitility.await().until(process::isAlive);
        // net.ipv4.tcp_syn_retries = 6
        // sysctl net.ipv4.tcp_syncookies
        // sysctl net.ipv4.tcp_syncookies=1
//        execPipe(sudoPipe("sysctl -a | grep tcp_syn_retries")).waitFor();
//        execPipe(sudoPipe("sysctl net.ipv4.tcp_syn_retries=1")).waitFor();
        new Thread(Unchecked.runnable(() -> execPipe("netstat -nat | grep -E '" + port + "'").waitFor()));
        Exception exception = Assertions.assertThrows(Exception.class, () -> new Socket("localhost", port), "Operation timed out");
        log.debug("ConnectException", exception);

        Socket accept = serverSocket.accept();
        Assertions.assertDoesNotThrow(() -> new Socket("localhost", port));
    }

    @Test
//    @SneakyThrows
    void backlog2() {

    }

}
