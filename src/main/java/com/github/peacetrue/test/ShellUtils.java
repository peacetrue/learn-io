package com.github.peacetrue.test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.lang.management.ManagementFactory;

import static com.github.peacetrue.test.ProcessBuilderUtils.sh;

/**
 * shell 工具类。
 *
 * @author peace
 */
@Slf4j
public class ShellUtils {

    private ShellUtils() {
    }

    /**
     * 获取当前 java 进程的编号。
     *
     * @return 进程号
     */
    public static int getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        log.debug("name: {}", name);
        return Integer.parseInt(name.split("@")[0]);
    }

    @SneakyThrows
    public static Process lsofJava(String... commands) {
//        return ProcessBuilderUtils.exec(sh(String.format("lsof -p %s | grep -E '  \\d{1,3}[ rwu]'", getPid())));
//        return ProcessBuilderUtils.exec(sh(String.format("lsof -p %s | grep -E '  \\d{3}[ rwu]'", getPid())));
        return Runtime.getRuntime().exec(sh(String.format("lsof -nP -p %s %s", getPid(), String.join(" ", commands))));
    }

    @SneakyThrows
    public static String output(Process process) {
        int exitValue = process.waitFor();
        if (exitValue != 0) throw new IllegalStateException(String.valueOf(exitValue));
        return IOUtils.toString(process.getInputStream());
    }

    public static void info(String title, Process process) {
        log.info("{}:\n{}", title, output(process));
    }

}
