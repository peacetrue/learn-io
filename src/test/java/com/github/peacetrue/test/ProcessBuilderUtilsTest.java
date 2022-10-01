package com.github.peacetrue.test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

/**
 * @author peace
 **/
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProcessBuilderUtilsTest {

    @Test
    void sh() {
        String command = "ls";
        Assertions.assertArrayEquals(new String[]{"/bin/sh", "-c", command}, ProcessBuilderUtils.sh(command));
    }

    @Test
    void sudo() {
        String command = "ls";
        Assertions.assertArrayEquals(new String[]{"sudo", "-S", command}, ProcessBuilderUtils.sudo(command));
    }

    @Test
    @Order(10)
    void sudoPipe() {
        String command = "ls";
        Assertions.assertArrayEquals(new String[]{"echo", "123456", "|", "sudo", "-S", command}, ProcessBuilderUtils.sudoPipe(command));
    }

    @Test
    void concat() {
        String command = "ls";
        String[] commands = ProcessBuilderUtils.<String[]>concat(ProcessBuilderUtils::sh, ProcessBuilderUtils::sudoPipe).apply(new String[]{command});
        Assertions.assertArrayEquals(new String[]{"/bin/sh", "-c", "echo 123456 | sudo -S " + command}, commands);
    }

    @Test
    @SneakyThrows
    void exec() {
        Assertions.assertEquals(0, ProcessBuilderUtils.exec("echo", "1").waitFor());
        Assertions.assertEquals(0, ProcessBuilderUtils.exec(ProcessBuilderUtils.sh("echo", "1", "|", "echo")).waitFor());
        Assertions.assertEquals(0, ProcessBuilderUtils.exec(ProcessBuilderUtils.sh(ProcessBuilderUtils.sudoPipe("echo", "1", "|", "echo"))).waitFor());
    }

    @Test
    @Order(Integer.MAX_VALUE)
    @SneakyThrows
    void execSudo() {
        System.setProperty("SUDO_PASS", SourcePathUtils.getTestResourceAbsolutePath("/pass.txt"));
        Assertions.assertEquals(0, ProcessBuilderUtils.execSudo("echo", "11").waitFor());
        Assertions.assertEquals(0, ProcessBuilderUtils.execSudo("tcpdump", "-c", "1").waitFor());
    }

}
