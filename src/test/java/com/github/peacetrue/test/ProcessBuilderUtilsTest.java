package com.github.peacetrue.test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

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
        String[] commands = ProcessBuilderUtilsTest.<String[]>concat(ProcessBuilderUtils::sh, ProcessBuilderUtils::sudoPipe).apply(new String[]{command});
        Assertions.assertArrayEquals(new String[]{"/bin/sh", "-c", "echo 123456 | sudo -S " + command}, commands);
    }

    @SafeVarargs
    public static <T> UnaryOperator<T> concat(UnaryOperator<T>... operators) {
        return Stream.of(operators).reduce(UnaryOperator.identity(), (left, right) -> v -> left.apply(right.apply(v)));
    }


    @Test
    @SneakyThrows
    void exec() {
        Assertions.assertEquals(0, ProcessBuilderUtils.exec("echo", "1").waitFor());
        Assertions.assertEquals(0, ProcessBuilderUtils.exec(ProcessBuilderUtils.sh("echo", "1", "|", "echo")).waitFor());
        Assertions.assertEquals(0, ProcessBuilderUtils.exec(ProcessBuilderUtils.sh(ProcessBuilderUtils.sudoPipe("echo", "1", "|", "echo"))).waitFor());
    }

    @Test
    @SneakyThrows
    void echoContinue() {
        Process process = new ProcessBuilder()
                .directory(new File(SourcePathUtils.getTestResourceAbsolutePath("/")))
                .command("echo-continue.sh")
                .inheritIO()
                .start();
        Assertions.assertEquals(0, process.waitFor());
    }
}
