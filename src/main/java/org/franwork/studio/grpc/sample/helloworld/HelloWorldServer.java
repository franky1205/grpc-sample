package org.franwork.studio.grpc.sample.helloworld;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Frankie Chao by 2020-05-27.
 */
public class HelloWorldServer {

    private Server server;

    public void start() throws IOException {
        this.server = ServerBuilder.forPort(8888)
                .addService(new GreeterServiceImpl())
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                HelloWorldServer.this.stop();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("Server shutdown successfully.");
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        HelloWorldServer helloWorldServer = new HelloWorldServer();
        helloWorldServer.start();
        helloWorldServer.blockUntilShutdown();
    }
}
