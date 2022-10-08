package com.github.peacetrue.java.nio;

import com.github.peacetrue.test.ShellUtils;
import com.github.peacetrue.test.SourcePathUtils;
import com.github.peacetrue.util.FileUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class ByteBufferTest {

    @SneakyThrows
    @Test
    void basic() {
        String byteBufferPath = SourcePathUtils.getTestResourceAbsolutePath("/byte-buffer");
        FileUtils.createFolderIfAbsent(Paths.get(byteBufferPath));

        Files.write(Paths.get(byteBufferPath + "/source.txt"), ShellUtils.output(ShellUtils.lsofJava()).getBytes(StandardCharsets.UTF_8));

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(10);
        byteBuffer.put((byte) 1);
        byte b = byteBuffer.get(0);

        Files.write(Paths.get(byteBufferPath + "/target.txt"), ShellUtils.output(ShellUtils.lsofJava()).getBytes(StandardCharsets.UTF_8));
    }
}
