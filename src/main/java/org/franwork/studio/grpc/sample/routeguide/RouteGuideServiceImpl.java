package org.franwork.studio.grpc.sample.routeguide;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Frankie Chao by 2020-05-25.
 */
@Slf4j
public class RouteGuideServiceImpl extends RouteGuideGrpc.RouteGuideImplBase {

    private AtomicInteger counter = new AtomicInteger();

    @Override
    public void getFeature(RouteGuideProto.Point request,
                           StreamObserver<RouteGuideProto.Feature> responseObserver) {
        log.info("Get getFeature request with Point: [{}]", request.getLatitude() + " " + request.getLongitude());
        responseObserver.onNext(this.findFeature(request));
        responseObserver.onCompleted();
    }

    @Override
    public void listFeatures(RouteGuideProto.Rectangle request, StreamObserver<RouteGuideProto.Feature> responseObserver) {
        log.info("Get listFeature request with Rectangle: [{}]", request);
        try {
            responseObserver.onNext(findFeature(request.getPoint1()));
            TimeUnit.SECONDS.sleep(3L);
            responseObserver.onNext(findFeature(request.getPoint2()));
            TimeUnit.SECONDS.sleep(3L);
            responseObserver.onNext(findFeature(request.getPoint3()));
            TimeUnit.SECONDS.sleep(3L);
            responseObserver.onNext(findFeature(request.getPoint4()));
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public StreamObserver<RouteGuideProto.Point> updatePoints(final StreamObserver<RouteGuideProto.Feature> responseObserver) {
        return new StreamObserver<RouteGuideProto.Point>() {

            private AtomicInteger receiveNumber = new AtomicInteger();

            @Override
            public void onNext(RouteGuideProto.Point value) {
                log.info("Get Point request from client: [{}]", value);
                receiveNumber.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {
                log.error("Get error from client with message: [{}]", t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                log.info("Get Point request completed signal return server response.");
                responseObserver.onNext(RouteGuideProto.Feature.newBuilder()
                        .setName("1. Receive all the points with number: [" + receiveNumber.intValue() + "]")
                        .build());
            }
        };
    }

    @Override
    public StreamObserver<RouteGuideProto.Point> getFeaturesStreamingly(StreamObserver<RouteGuideProto.Feature> responseObserver) {
        return new StreamObserver<RouteGuideProto.Point>() {

            private AtomicInteger receiveNumber = new AtomicInteger();

            @Override
            public void onNext(RouteGuideProto.Point value) {
                log.info("Get Point request from client: [{}]", value);
                receiveNumber.incrementAndGet();
                responseObserver.onNext(RouteGuideProto.Feature.newBuilder()
                        .setName("Receive all the points with number: [" + receiveNumber.intValue() + "]")
                        .build());
            }

            @Override
            public void onError(Throwable t) {
                log.error("Get error from client with message: [{}]", t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                log.info("Get Point request completed signal return server complete signal.");
                responseObserver.onCompleted();
            }
        };
    }

    private RouteGuideProto.Feature findFeature(RouteGuideProto.Point point) {
        return RouteGuideProto.Feature.newBuilder()
                .setName("Server-Feature-Reply-" + counter.getAndIncrement())
                .setLocation(point)
                .build();
    }
}
