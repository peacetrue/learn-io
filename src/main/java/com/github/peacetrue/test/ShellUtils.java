package com.github.peacetrue.test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;

import static com.github.peacetrue.test.ProcessBuilderUtils.exec;
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
        return Runtime.getRuntime().exec(sh(String.format("lsof -p %s %s", getPid(), String.join(" ", commands))));
    }

}