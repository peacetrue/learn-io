package com.github.peacetrue.java.io;

import com.github.peacetrue.test.ShellUtilsTest;
import com.github.peacetrue.test.SourcePathUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;

/**
 * @author peace
 **/
@Slf4j
@EnabledOnOs(OS.LINUX)
public class RandomAccessFileTest {

    @Test
    @SneakyThrows
    void basic() {
        ShellUtilsTest.lsof();

        String path = SourcePathUtils.getTestResourceAbsolutePath("/RandomAccessFile.txt");
        try (RandomAccessFile file = new RandomAccessFile(path, "rw")) {
            FileDescriptor fileFD = file.getFD();
            String filter = filter(fileFD);
            ShellUtilsTest.lsof(filter);

            Assertions.assertEquals(0, file.getFilePointer());
            byte aByte = file.readByte();
            log.info("aByte: {}", (char) aByte);
            Assertions.assertEquals(1, file.getFilePointer());

            file.seek(10);
            Assertions.assertEquals(10, file.getFilePointer());

            ShellUtilsTest.lsof(filter);
        }
    }

    public static String filter(FileDescriptor fileFD) {
        Object value = fd(fileFD);
        log.info("fd: {}", value);
        return String.format("-o | grep '%s'", value);
    }

    @SneakyThrows
    public static Object fd(FileDescriptor fileFD) {
        Field fd = FileDescriptor.class.getDeclaredField("fd");
        fd.setAccessible(true);
        return fd.get(fileFD);
    }
}
