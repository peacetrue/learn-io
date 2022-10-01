package com.github.peacetrue.learn.shell;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static com.github.peacetrue.test.ProcessBuilderUtils.exec;

/**
 * strace、dtruss
 *
 * @author peace
 **/
@EnabledOnOs(OS.LINUX)
public class StraceTest {

    @Test
    @SneakyThrows
    void basic() {
        Assertions.assertEquals(0, exec("strace java --version").waitFor());
    }

}
