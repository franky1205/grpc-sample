package org.franwork.studio.grpc.sample.routeguide;

import io.grpc.stub.StreamObserver;
import org.franwork.studio.grpc.sample.routeguide.RouteGuideProto.Feature;
import org.franwork.studio.grpc.sample.routeguide.RouteGuideProto.Point;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Frankie Chao by 2020-06-01.
 */
public class RouteGuideStreamRequestObserver extends StreamRequestObserver<Point, Feature> {

    private final AtomicInteger counter = new AtomicInteger(1);

    RouteGuideStreamRequestObserver(StreamObserver<Feature> responseStreamObserver) {
        super(responseStreamObserver);
    }

    @Override
    protected Feature getResponse(Point request) {
        return Feature.newBuilder()
                .setName("StreamObserver-Reply-" + counter.getAndIncrement())
                .setLocation(request)
                .build();
    }
}
