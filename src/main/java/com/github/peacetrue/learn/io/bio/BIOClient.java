package com.github.peacetrue.learn.io.bio;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.fi.util.function.CheckedBiConsumer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * @author peace
 **/
@Slf4j
@Data
public class BIOClient {

    /** 客户端配置 */
    private SocketProperties clientProperties;
    /** 服务调用者 */
    private CheckedBiConsumer<OutputStream, InputStream> invoker;

    public BIOClient(SocketProperties clientProperties, CheckedBiConsumer<OutputStream, InputStream> invoker) {
        this.clientProperties = Objects.requireNonNull(clientProperties);
        this.invoker = Objects.requireNonNull(invoker);
    }

    @SneakyThrows
    public void start() {
        try (Socket socket = new Socket(clientProperties.getHost(), clientProperties.getPort())) {
            BIOServer.setup(socket, clientProperties);
            try (OutputStream outputStream = socket.getOutputStream();
                 InputStream inputStream = socket.getInputStream()) {
                log.info("invoke");
                invoker.accept(outputStream, inputStream);
            }
        }
    }

}
