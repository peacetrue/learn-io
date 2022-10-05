package com.github.peacetrue.java.io;

import com.github.peacetrue.test.SourcePathUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.RandomAccessFile;

/**
 * @author peace
 **/
@Slf4j
@EnabledOnOs(OS.LINUX)
public class RandomAccessFileTest {

    @Test
    @SneakyThrows
    void basic() {
        String path = SourcePathUtils.getTestResourceAbsolutePath("/RandomAccessFile.txt");
        Assertions.assertEquals(0, FileTest.lsof().waitFor());
        RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
        Assertions.assertEquals(0, FileTest.lsof().waitFor());
    }
}
