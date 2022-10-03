package com.github.peacetrue.test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.peacetrue.test.ProcessBuilderUtils.exec;
import static com.github.peacetrue.test.ProcessBuilderUtils.sh;

@Slf4j
public class ShellUtilsTest {

    @Test
    void getPid() {
        Assertions.assertDoesNotThrow(ShellUtils::getPid);
    }

    @SneakyThrows
    @Test
    void lsof$$() {
        Assertions.assertEquals(0, exec(sh("lsof -p $$")).waitFor());
    }

//    @Test
    @SneakyThrows
    void lsofJava() {
        // lsof -p $$ | grep -E ' [[:digit:]]{1,3}[ rwu]'
        lsof();
    }

    @SneakyThrows
    public static void lsof(String... commands) {
        Process process = ShellUtils.lsofJava(commands);
        Assertions.assertEquals(0, process.waitFor());
        log.info("lsof:\n{}", IOUtils.toString(process.getInputStream()));
    }
}