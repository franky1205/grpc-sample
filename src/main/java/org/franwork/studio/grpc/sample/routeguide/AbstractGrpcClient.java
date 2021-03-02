package org.franwork.studio.grpc.sample.routeguide;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author Frankie Chao by 2021-02-19.
 */
@Slf4j
abstract class AbstractGrpcClient {

    final ManagedChannel managedChannel;

    AbstractGrpcClient(String serverHost, int port) {
        this.managedChannel = ManagedChannelBuilder
                .forAddress(serverHost, port)
                .usePlaintext()
                .build();
        this.addShutdownHook();
        ConnectivityState initialState = managedChannel.getState(true);
        log.info("The initial connectivity state between client and server: " + initialState);
        this.connectivityStateChanged(initialState);
        managedChannel.notifyWhenStateChanged(initialState, this::runnableWhenStateChanged);
    }

    private void runnableWhenStateChanged() {
        ConnectivityState connectivityState = managedChannel.getState(true);
        this.connectivityStateChanged(connectivityState);
        managedChannel.notifyWhenStateChanged(connectivityState, this::runnableWhenStateChanged);
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownQuietly));
    }

    private void shutdownQuietly() {
        try {
            long startTime = System.currentTimeMillis();
            managedChannel.shutdown();
            managedChannel.awaitTermination(5, TimeUnit.SECONDS);
            log.info("gRPC client shutdown successfully in {} milliseconds.", (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            log.error("Shutdown gRPC client with error: [{}]", e.getMessage(), e);
        }
    }

    abstract void connectivityStateChanged(ConnectivityState connectivityState);
}
