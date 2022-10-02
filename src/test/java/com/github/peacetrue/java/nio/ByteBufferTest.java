package com.github.peacetrue.java.nio;

import com.github.peacetrue.test.ShellUtilsTest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

public class ByteBufferTest {

    @Test
    void basic() {
        ShellUtilsTest.lsof();
        ByteBuffer buffer = ByteBuffer.allocate(10);
        ShellUtilsTest.lsof();
    }
}
