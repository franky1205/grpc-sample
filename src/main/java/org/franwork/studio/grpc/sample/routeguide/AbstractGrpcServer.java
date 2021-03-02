package org.franwork.studio.grpc.sample.routeguide;

import io.grpc.Server;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

/**
 * @author Frankie Chao by 2020-06-03.
 */
@Slf4j
abstract class AbstractGrpcServer {

    private final Server server;

    AbstractGrpcServer(@NotNull Server server) {
        this.server = server;
        this.addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            long startTime = System.currentTimeMillis();
            try {
                AbstractGrpcServer.this.stop();
                log.info("Server shutdown successfully in {} milliseconds.", (System.currentTimeMillis() - startTime));
            } catch (Exception ex) {
                log.error("Stop the gRPC server with error: [{}]", ex.getMessage(), ex);
            }
        }));
    }

    private void stop() throws InterruptedException {
        this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }

    public void blockUntilShutdown() throws InterruptedException {
        this.server.awaitTermination();
    }
}
