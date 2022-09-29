package com.github.peacetrue.learn.lang;

import com.github.peacetrue.test.SourcePathUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author peace
 * @see <a href="https://stackoverflow.com/questions/3776195/using-java-processbuilder-to-execute-a-piped-command">ProcessBuilder 如何支持管道命令</a>
 **/
@Slf4j
public class ProcessTest {

    @Test
    @SneakyThrows
    void processBuilder() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.inheritIO().command("ls");
        Process process = processBuilder.start();
        Assertions.assertEquals(0, process.waitFor());
    }

    @Test
    @SneakyThrows
    void runtime() {
        // 看不到输出结果
        int waitFor = Runtime.getRuntime().exec("ls").waitFor();
        Assertions.assertEquals(0, waitFor, "runtime");
    }

    @Test
    @SneakyThrows
    void outputToFile() {
        String path = SourcePathUtils.getTestResourceAbsolutePath("/temp.txt");
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectOutput(new File(path)).command("ls");
        Process process = processBuilder.start();
        Assertions.assertEquals(0, process.waitFor());
    }

    @Test
    @SneakyThrows
    void sudoMissingPassword() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.inheritIO();
        processBuilder.command("sudo", "ls");
        Process process = processBuilder.start();
        Assertions.assertNotEquals(0, process.waitFor(), "无法输入密码而失败");
        // sudo: a terminal is required to read the password; either use the -S option to read from standard input or configure an askpass helper
        // sudo: a password is required
    }

    @Test
    @SneakyThrows
    void sudoByPIPE() {
        // 默认输出到管道，使用管道符后，输出到控制台失效
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.inheritIO();
        Process process = processBuilder.command("echo", "123456", "|", "sudo", "-S", "ls").start();
        Assertions.assertEquals(0, process.waitFor(), "使用 -S 选项设置密码");
    }

    @Test
    @SneakyThrows
    void sudoByFile() {
        String path = SourcePathUtils.getTestResourceAbsolutePath("/pass.txt");
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectInput(new File(path)).redirectOutput(ProcessBuilder.Redirect.INHERIT);
        Process process = processBuilder.command("sudo", "-S", "ls").start();
        Assertions.assertEquals(0, process.waitFor(), "使用 -S 选项设置密码");
    }

}
