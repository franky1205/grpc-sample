package org.franwork.studio.grpc.sample.routeguide;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ConnectivityState;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Frankie Chao by 2020-05-07.
 */
@Slf4j
public class RouteGuideClient extends AbstractGrpcClient {

    private final static Executor taskExecutor = Executors.newFixedThreadPool(5);

    private RouteGuideGrpc.RouteGuideFutureStub routeGuideFutureStub;

    private RouteGuideGrpc.RouteGuideStub routeGuideStub;

    private RouteGuideClient(String serverHost, int port) {
        super(serverHost, port);
    }

    @Override
    void connectivityStateChanged(ConnectivityState connectivityState) {
        log.info("ConnectivityState changed to [{}] between client and server host.", connectivityState);
        if (ConnectivityState.CONNECTING == connectivityState) {
            this.startSendingMessage();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new RouteGuideClient("localhost", 8989);
        TimeUnit.SECONDS.sleep(1000L);
    }

    private void startSendingMessage() {
        routeGuideFutureStub = RouteGuideGrpc.newFutureStub(managedChannel);
        routeGuideStub = RouteGuideGrpc.newStub(managedChannel);
//        sendUnaryMessage();
//        sendServerStreamingMessage();
        sendClientStreamingMessage();
//        sendBidirectionStreamingMessage();
    }

    private void sendUnaryMessage() {
        final ListenableFuture<RouteGuideProto.Feature> featureFuture = routeGuideFutureStub.getFeature(RouteGuideProto.Point.newBuilder()
                .setLatitude(123)
                .setLongitude(456)
                .build());
        featureFuture.addListener(() -> {
            try {
                RouteGuideProto.Feature feature = featureFuture.get();
                log.info("==========> Get FEATURE from server with Unary request: [{}]", feature);
            } catch (Exception e) {
                log.error("Get feature from future with error: [{}]", e.getMessage(), e);
            }
        }, taskExecutor);
    }

    private void sendServerStreamingMessage() {
        this.routeGuideStub.listFeatures(RouteGuideProto.Rectangle.newBuilder()
                .setPoint1(RouteGuideProto.Point.newBuilder().setLatitude(1).setLongitude(2).build())
                .setPoint2(RouteGuideProto.Point.newBuilder().setLatitude(3).setLongitude(4).build())
                .setPoint3(RouteGuideProto.Point.newBuilder().setLatitude(5).setLongitude(6).build())
                .setPoint4(RouteGuideProto.Point.newBuilder().setLatitude(7).setLongitude(8).build())
                .build(), new StreamObserver<RouteGuideProto.Feature>() {
            @Override
            public void onNext(RouteGuideProto.Feature feature) {
                log.info("==========> Get streaming Feature: [{}]", feature);
            }

            @Override
            public void onError(Throwable t) {
                log.error(">>>>>>>>>> Get streaming error: [{}]", t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                log.info("####### Server Streaming Feature completed #######");
            }
        });
    }

    private void sendClientStreamingMessage() {
        StreamObserver<RouteGuideProto.Point> pointStreamObserver = this.routeGuideStub.updatePoints(
                new StreamObserver<RouteGuideProto.Feature>() {
                    @Override
                    public void onNext(RouteGuideProto.Feature value) {
                        log.info("==========> Get server reply Feature: [{}]", value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error(">>>>>>>>>> Get server reply error: [{}]", t.getMessage(), t);
                    }

                    @Override
                    public void onCompleted() {
                        log.info("####### Server reply Feature completed #######");
                    }
                });
        try {
            pointStreamObserver.onNext(RouteGuideProto.Point.newBuilder()
                    .setLatitude(555)
                    .setLongitude(666)
                    .build());
            TimeUnit.SECONDS.sleep(3L);
            pointStreamObserver.onNext(RouteGuideProto.Point.newBuilder()
                    .setLatitude(777)
                    .setLongitude(888)
                    .build());
            TimeUnit.SECONDS.sleep(3L);
            pointStreamObserver.onNext(RouteGuideProto.Point.newBuilder()
                    .setLatitude(999)
                    .setLongitude(000)
                    .build());
            pointStreamObserver.onCompleted();
        } catch (InterruptedException e) {
            pointStreamObserver.onError(e);
        }
    }

    private void sendBidirectionStreamingMessage() {
        StreamObserver<RouteGuideProto.Point> pointStreamObserver = this.routeGuideStub.getFeaturesStreamingly(
                new StreamObserver<RouteGuideProto.Feature>() {
            @Override
            public void onNext(RouteGuideProto.Feature value) {
                log.info("==========> Get server stream Feature reply: [{}]", value);
            }

            @Override
            public void onError(Throwable t) {
                log.error(">>>>>>>>>> Get server stream Feature reply error: [{}]", t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                log.info("####### Server stream Feature reply completed #######");
            }
        });
        try {
            TimeUnit.SECONDS.sleep(10L);
            pointStreamObserver.onNext(RouteGuideProto.Point.newBuilder()
                    .setLatitude(1111)
                    .setLongitude(2222)
                    .build());
            TimeUnit.SECONDS.sleep(10L);
            pointStreamObserver.onNext(RouteGuideProto.Point.newBuilder()
                    .setLatitude(3333)
                    .setLongitude(4444)
                    .build());
            TimeUnit.SECONDS.sleep(10L);
            pointStreamObserver.onNext(RouteGuideProto.Point.newBuilder()
                    .setLatitude(5555)
                    .setLongitude(6666)
                    .build());
            pointStreamObserver.onCompleted();
        } catch (InterruptedException e) {
            pointStreamObserver.onError(e);
        }
    }
}
