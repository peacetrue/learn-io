package com.github.peacetrue.java.io;

import com.github.peacetrue.test.SourcePathUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author peace
 **/
@Slf4j
public class RandomAccessFileTest {

    @Test
    @SneakyThrows
    void name() {
        String path = SourcePathUtils.getTestResourceAbsolutePath("/RandomAccessFile.txt");
        Files.write(Paths.get(path), "1".getBytes(StandardCharsets.UTF_8));
        FileInputStream fileInputStream = new FileInputStream(path);
        byte[] bytes = fileInputStream.readAllBytes();
        System.out.println(new String(bytes));
        FileTest.lsof().waitFor();
//        RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
//        randomAccessFile.
    }
}
