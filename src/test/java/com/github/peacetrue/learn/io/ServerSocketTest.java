package com.github.peacetrue.learn.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.BindException;
import java.net.ServerSocket;

/**
 * @author peace
 **/
public class ServerSocketTest {

    @Test
    void name() {
        Assertions.assertThrows(Exception.class, () -> new ServerSocket(1));
    }
}
