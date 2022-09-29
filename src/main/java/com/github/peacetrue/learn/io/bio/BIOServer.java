package com.github.peacetrue.learn.io.bio;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedBiConsumer;
import org.jooq.lambda.fi.util.function.CheckedConsumer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * @author peace
 **/
@Slf4j
@Data
public class BIOServer {

    /** 服务端配置 */
    private SocketProperties serverProperties;
    /** 客户端配置 */
    private SocketProperties clientProperties;
    /** 线程池执行器 */
    private Executor executor = Executors.newSingleThreadExecutor();
    /** 服务调用者 */
    private CheckedBiConsumer<InputStream, OutputStream> invoker = IOUtils::copy;
    /** 是否运行中 */
    private final AtomicBoolean running = new AtomicBoolean(false);
    /** 服务端 */
    private ServerSocket serverSocket;

    public BIOServer(SocketProperties serverProperties, SocketProperties clientProperties) {
        this.serverProperties = Objects.requireNonNull(serverProperties);
        this.clientProperties = Objects.requireNonNull(clientProperties);
    }

    @SneakyThrows
    public void start() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Server is running!");
            return;
        }

        try (ServerSocket localServerSocket = new ServerSocket()) {
            setup(localServerSocket);
            log.info("Server started up at: {}", localServerSocket.getLocalSocketAddress());
            this.serverSocket = localServerSocket;
            while (running.get()) {
                work(localServerSocket.accept());
            }
        } catch (Exception exception) {
            running.set(false);
            throw exception;
        }
    }

    public Thread startSilently() {
        Thread thread = new Thread(this::start);
        thread.start();
        return thread;
    }

    @SneakyThrows
    void setup(ServerSocket serverSocket) {
        log.info("setup Server: {}", serverSocket);

        InetSocketAddress endpoint = getEndpoint();
        log.debug("endpoint: {}", endpoint);
        if (serverProperties.getBacklog() == null) {
            serverSocket.bind(endpoint);
        } else {
            serverSocket.bind(endpoint, serverProperties.getBacklog());
        }
        setSafely(serverProperties::getReceiveBufferSize, serverSocket::setReceiveBufferSize);
        setSafely(serverProperties::getReuseAddress, serverSocket::setReuseAddress);
        setSafely(serverProperties::getSoTimeout, serverSocket::setSoTimeout);
        Optional.ofNullable(serverProperties.getPerformancePreferences()).ifPresent(preferences ->
                serverSocket.setPerformancePreferences(preferences.getConnectionTime(), preferences.getLatency(), preferences.getBandwidth())
        );
    }

    @SneakyThrows
    public void work(Socket clientSocket) {
        log.info("Client connecting from: {}", clientSocket.getLocalSocketAddress());
        executor.execute(Unchecked.runnable(() -> {
            setup(clientSocket, clientProperties);
            try (InputStream inputStream = clientSocket.getInputStream();
                 OutputStream outputStream = clientSocket.getOutputStream()) {
                invoker.accept(inputStream, outputStream);
            }
        }));
    }

    static void setup(Socket clientSocket, SocketProperties clientProperties) {
        setSafely(clientProperties::getKeepalive, clientSocket::setKeepAlive);
        setSafely(clientProperties::getReceiveBufferSize, clientSocket::setReceiveBufferSize);
        setSafely(clientProperties::getReuseAddress, clientSocket::setReuseAddress);
        setSafely(clientProperties::getSoTimeout, clientSocket::setSoTimeout);
    }

    private InetSocketAddress getEndpoint() {
        return serverProperties.getHost() == null
                ? new InetSocketAddress(serverProperties.getPort())
                : new InetSocketAddress(serverProperties.getHost(), serverProperties.getPort());
    }

    private static <T> void setSafely(Supplier<T> supplier, CheckedConsumer<T> consumer) {
        Optional.ofNullable(supplier.get()).ifPresent(Unchecked.consumer(consumer));
    }

    public boolean stop() {
        return running.compareAndSet(true, false);
    }

}
