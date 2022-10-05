package com.github.peacetrue.java.io;

import com.github.peacetrue.test.ProcessBuilderUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;

import static com.github.peacetrue.test.ProcessBuilderUtils.sh;

/**
 * @author peace
 **/
@Slf4j
class FileTest {


    @Test
    @SneakyThrows
    void exec() {
//        String path = SourcePathUtils.getTestResourceAbsolutePath("/logback.xml");
//        File file = new File(path);
        lsof().waitFor();
    }

    public static Process lsof() {
//        return ProcessBuilderUtils.exec(sh(String.format("lsof -p %s | grep -E '  \\d{1,3}[ rwu]'", getPid())));
        return ProcessBuilderUtils.exec(sh(String.format("lsof -p %s | grep -E '  \\d{3}[ rwu]'", getPid())));
    }

    public static int getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        log.debug("name: {}", name);
        int pid = Integer.parseInt(name.split("@")[0]);
        log.debug("pid: {}", pid);
        return pid;
    }
}
