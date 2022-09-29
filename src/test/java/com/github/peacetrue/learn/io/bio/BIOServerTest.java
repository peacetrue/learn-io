package com.github.peacetrue.learn.io.bio;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

/**
 * @author peace
 **/
@Slf4j
class BIOServerTest {

    @Test
    void start() throws InterruptedException {
        BIOServer bioServer = new BIOServer(
                SocketProperties.builder().port(0).build(),
                SocketProperties.builder().build()
        );
        bioServer.setInvoker((in, out) -> {
            String request = IOUtils.toString(in);
            log.info("request: {}", request);
            IOUtils.write(request, out);
        });
        bioServer.startSilently();
        Awaitility.await().forever().until(() -> bioServer.getServerSocket() != null);

        int localPort = bioServer.getServerSocket().getLocalPort();
        new BIOClient(
                SocketProperties.builder().port(localPort).build(),
                (out, in) -> {
                    IOUtils.write("echo", out);
//                    log.info("response: {}", IOUtils.toString(in));
                }
        ).start();
    }
}
