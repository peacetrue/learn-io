package com.github.peacetrue.test;

import com.google.common.collect.ObjectArrays;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

/**
 * ProcessBuilder 工具类。
 *
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
        String command = String.join(" ", commands);
        log.debug("sh: /bin/sh -c \"{}\"", command);
        return new String[]{"/bin/sh", "-c", command};
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
     * 使用 sudo 以管道方式执行命令（无需额外读取密码）。
     *
     * @param commands 原始命令
     * @return sudo 包装的命令
     */
    public static String[] sudoPipe(String... commands) {
        return ObjectArrays.concat(new String[]{"echo", sudoPasswordValue(), "|"}, sudo(commands), String.class);
    }

    /**
     * 获取 sudo 密码值。
     * 优先读取环境变量 SUDO_PASS，
     * 其次读取属性变量 SUDO_PASS，
     * 最后默认为 123456。
     *
     * @return sudo 密码值
     */
    public static String sudoPasswordValue() {
        return Objects.toString(System.getenv("SUDO_PASS"), System.getProperty("SUDO_PASS", "123456"));
    }

    /**
     * 执行不可含管道的命令。
     *
     * @param commands 命令（不可含管道）
     * @return 进程
     */
    @SneakyThrows
    public static Process exec(String... commands) {
        log.debug("exec command: {}", String.join(" ", commands));
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commands);
        processBuilder.inheritIO();
        return processBuilder.start();
    }

    /**
     * 执行可含管道的命令。
     *
     * @param commands 命令（可含管道）
     * @return 进程
     */
    public static Process execPipe(String... commands) {
        log.debug("exec command(pipe): {}", String.join(" ", commands));
        return exec(sh(commands));
    }

    /**
     * 执行 sudo 命令。
     *
     * @param commands 命令
     * @return 进程
     */
    public static Process execSudo(String... commands) {
        return execSudo(new File(sudoPasswordPath()), commands);
    }

    /**
     * 获取 sudo 密码文件路径。
     *
     * @return sudo 密码文件路径
     */
    public static String sudoPasswordPath() {
        return Objects.toString(System.getenv("SUDO_PASS"), System.getProperty("SUDO_PASS", "SUDO_PASS"));
    }

    /**
     * 执行 sudo 命令。
     *
     * @param file     存储 sudo 密码的文件
     * @param commands 命令
     * @return 进程
     */
    @SneakyThrows
    public static Process execSudo(File file, String... commands) {
        commands = sudo(commands);
        log.debug("exec command(sudo): {}", Arrays.toString(commands));
        ProcessBuilder processBuilder = new ProcessBuilder(sudo(commands));
        processBuilder.redirectInput(file)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
        ;
        return processBuilder.start();
    }
}
