package com.github.peacetrue.learn.shell;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.OS;

import static com.github.peacetrue.test.ProcessBuilderUtils.execPipe;
import static com.github.peacetrue.test.ProcessBuilderUtils.sudoPipe;

/**
 * @author peace
 **/
public class StraceTest {

    @Test
    @SneakyThrows
    void basic() {
        int exitValue = execPipe(sudoPipe(strace() + " java --version")).waitFor();
        Assertions.assertEquals(0, exitValue);
    }

    private static String strace() {
        return OS.MAC.isCurrentOs() ? "dtruss" : "strace";
    }
}
