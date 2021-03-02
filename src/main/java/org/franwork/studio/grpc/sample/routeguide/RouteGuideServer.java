package org.franwork.studio.grpc.sample.routeguide;

import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author Frankie Chao by 2020-05-07.
 */
@Slf4j
public class RouteGuideServer extends AbstractGrpcServer {

    RouteGuideServer(int port) throws IOException {
        super(ServerBuilder.forPort(port)
                .addService(new RouteGuideServiceImpl())
                .build()
                .start());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        RouteGuideServer routeGuideServer = new RouteGuideServer(8989);
        routeGuideServer.blockUntilShutdown();
    }
}
