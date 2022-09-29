package com.github.peacetrue.test;

import com.google.common.collect.ObjectArrays;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author peace
 * @see <a href="https://stackoverflow.com/questions/3776195/using-java-processbuilder-to-execute-a-piped-command">ProcessBuilder 如何支持管道命令</a>
 **/
@Slf4j
public class ProcessBuilderUtils {

    private ProcessBuilderUtils() {
    }

    /**
     * 使用 sh 执行命令。
     *
     * @param commands 原始命令
     * @return sh 包装的命令
     */
    public static String[] sh(String... commands) {
        return new String[]{"/bin/sh", "-c", String.join(" ", commands)};
    }

    /**
     * 使用 sudo 执行命令。
     *
     * @param commands 原始命令
     * @return sudo 包装的命令
     */
    public static String[] sudo(String... commands) {
        return ObjectArrays.concat(new String[]{"sudo", "-S"}, commands, String.class);
    }

    /**
     * 使用 sudo 执行命令。
     *
     * @param commands 原始命令
     * @return sudo 包装的命令
     */
    public static String[] sudoPipe(String... commands) {
        return ObjectArrays.concat(new String[]{"echo", sudoPasswordValue(), "|", "sudo", "-S"}, commands, String.class);
    }

    public static String sudoPasswordValue() {
        return Objects.toString(System.getenv("SUDO_ASKPASS"), System.getProperty("SUDO_ASKPASS", "123456"));
    }

    @SafeVarargs
    public static <T> UnaryOperator<T> concat(UnaryOperator<T>... operators) {
        return Stream.of(operators).reduce(UnaryOperator.identity(), (left, right) -> v -> left.apply(right.apply(v)));
    }

    @SneakyThrows
    public static Process exec(String... commands) {
        log.info("exec command: {}", String.join(" ", commands));
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.command(commands);
        processBuilder.inheritIO();
        return processBuilder.start();
    }

    public static Process execPipe(String... commands) {
        return exec(sh(commands));
    }

    public static Process execSudo(String... commands) {
        return execSudo(new File(sudoPasswordPath()), commands);
    }

    @SneakyThrows
    public static Process execSudo(File file, String... commands) {
        commands = sudo(commands);
        log.info("exec command(sudo): {}", Arrays.toString(commands));
        ProcessBuilder processBuilder = new ProcessBuilder(sudo(commands));
        processBuilder.redirectInput(file)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
        ;
        return processBuilder.start();
    }

    public static String sudoPasswordPath() {
        return Objects.toString(System.getenv("SUDO_ASKPASS"), System.getProperty("SUDO_ASKPASS", "./ASKPASS"));
    }
}
