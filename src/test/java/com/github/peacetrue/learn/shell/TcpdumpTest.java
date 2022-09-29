package com.github.peacetrue.learn.shell;

import com.github.peacetrue.test.ProcessBuilderUtils;
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
    void _D() {
        int exitValue = ProcessBuilderUtils.exec("tcpdump -D").waitFor();
        Assertions.assertEquals(0, exitValue);
    }

    @Test
    @SneakyThrows
    void _c() {
        int exitValue = ProcessBuilderUtils.exec("tcpdump -c 5").waitFor();
        Assertions.assertEquals(0, exitValue);
    }

    @Test
    @SneakyThrows
    void _i() {
        int exitValue = ProcessBuilderUtils.exec("tcpdump -i lo0 -c 5").waitFor();
        Assertions.assertEquals(0, exitValue);
    }

    @Test
    @SneakyThrows
    void filter() {
        int exitValue = ProcessBuilderUtils.exec("tcpdump -i lo0 -c 5 tcp").waitFor();
        Assertions.assertEquals(0, exitValue);
    }

    @Test
    @SneakyThrows
    void filterComb() {
        Thread thread = new Thread(Unchecked.runnable(() -> {
            int exitValue = ProcessBuilderUtils.exec("tcpdump -i lo0 -c 5 tcp and port 1000").waitFor();
            Assertions.assertEquals(0, exitValue);
        }));
        thread.start();

        while (thread.isAlive()) {
            Assertions.assertThrows(Exception.class, () -> new Socket("localhost", 1000));
        }
        thread.join();
    }


}
