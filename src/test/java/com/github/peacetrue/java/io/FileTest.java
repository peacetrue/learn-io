package com.github.peacetrue.java.io;

import com.github.peacetrue.test.ShellUtils;
import com.github.peacetrue.test.ShellUtilsTest;
import com.github.peacetrue.test.SourcePathUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * @author peace
 **/
@Slf4j
class FileTest {

    @Test
    @SneakyThrows
    void basic() {
        String path = SourcePathUtils.getTestResourceAbsolutePath("/RandomAccessFile.txt");
        File file = new File(path);
        ShellUtilsTest.lsof();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            ShellUtilsTest.lsof(RandomAccessFileTest.filter(fileInputStream.getFD()));
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            ShellUtilsTest.lsof(RandomAccessFileTest.filter(fileOutputStream.getFD()));
        }
    }

}
