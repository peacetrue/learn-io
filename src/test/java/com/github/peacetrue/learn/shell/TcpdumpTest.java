package com.github.peacetrue.learn.shell;

import lombok.SneakyThrows;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.Socket;

/**
 * tcpdump 测试。
 *
 * @author peace
 * @see <a href="https://community.helpsystems.com/forums/intermapper/intermapper-flows/729771e0-fa83-e511-80d0-005056842064">tcpdump</a>
 **/
public class TcpdumpTest {

    @Test
    @SneakyThrows
    void D() {
        int exitValue = Runtime.getRuntime().exec("tcpdump -D").waitFor();
        Assertions.assertEquals(0, exitValue);
    }

    @SneakyThrows
    void c() {
        int exitValue = Runtime.getRuntime().exec("tcpdump -c 5").waitFor();
        Assertions.assertEquals(0, exitValue);
    }

    @SneakyThrows
    void i() {
        int exitValue = Runtime.getRuntime().exec("tcpdump -i lo0 -c 5").waitFor();
        Assertions.assertEquals(0, exitValue);
    }

    @SneakyThrows
    void filter() {
        int exitValue = Runtime.getRuntime().exec("tcpdump -i lo0 -c 5 tcp").waitFor();
        Assertions.assertEquals(0, exitValue);
    }

    @SneakyThrows
    void filterComb() {
        Thread thread = new Thread(Unchecked.runnable(() -> {
            int exitValue = Runtime.getRuntime().exec("tcpdump -i lo0 -c 5 tcp and port 1000").waitFor();
            Assertions.assertEquals(0, exitValue);
        }));
        thread.start();

        while (thread.isAlive()) {
            Assertions.assertThrows(Exception.class, () -> new Socket("localhost", 1000));
        }
        thread.join();
    }


}
