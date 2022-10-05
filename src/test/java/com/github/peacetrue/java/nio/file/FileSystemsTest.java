package com.github.peacetrue.java.nio.file;

import com.github.peacetrue.spring.beans.BeanUtils;
import com.github.peacetrue.util.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

/**
 * @author peace
 **/
@Slf4j
public class FileSystemsTest {
    @Test
    void properties() {
        FileSystem fileSystem = FileSystems.getDefault();
        log.info("properties:{}", fileSystem.provider());
    }
}
