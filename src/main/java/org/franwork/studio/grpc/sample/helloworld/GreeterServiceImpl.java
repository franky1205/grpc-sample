package org.franwork.studio.grpc.sample.helloworld;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.franwork.studio.grpc.sample.helloworld.HelloWorldProto.*;

/**
 * @author Frankie Chao by 2020-05-27.
 */
@Slf4j
public class GreeterServiceImpl extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        responseObserver.onNext(HelloReply.newBuilder()
                .setMessage("Hello, " + request.getName() + ". This is the reply from Server.")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloRequest> sayHelloStreaming(StreamObserver<HelloReply> responseObserver) {
        final ServerCallStreamObserver<HelloReply> serverCallStreamObserver =
                (ServerCallStreamObserver<HelloReply>) responseObserver;
        serverCallStreamObserver.disableAutoInboundFlowControl();

        final OnReadyServerHandler onReadyServerHandler = new OnReadyServerHandler(serverCallStreamObserver);
        serverCallStreamObserver.setOnReadyHandler(onReadyServerHandler);

        // Give gRPC a StreamObserver that can observe and process incoming requests.
        return new StreamObserver<HelloRequest>() {
            @Override
            public void onNext(HelloRequest request) {
                // Process the request and send a response or an error.
                try {
                    log.info("--> Receive request name: [{}]", request.getName());

                    // Simulate server "work"
                    Thread.sleep(100);

                    // Send a response.
                    String message = "Hello " + request.getName();
                    log.info("<-- Reply with message: [{}]", message);
                    HelloReply reply = HelloReply.newBuilder().setMessage(message).build();
                    responseObserver.onNext(reply);

                    if (serverCallStreamObserver.isReady()) {
                        serverCallStreamObserver.request(1);
                    } else {
                        onReadyServerHandler.wasReady.set(false);
                    }

                } catch (Throwable ex) {
                    log.error("Process request with error: [{}]", ex.getMessage(), ex);
                    responseObserver.onError(ex);
                }
            }

            @Override
            public void onError(Throwable t) {
                // End the response stream if the client presents an error.
                log.error("Client present an error: [{}]. Complete the response stream.", t.getMessage(), t);
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                // Signal the end of work when the client ends the request stream.
                log.info("Client COMPLETES the request stream.");
                responseObserver.onCompleted();
            }
        };
    }

    public static class OnReadyServerHandler implements Runnable {

        private final ServerCallStreamObserver serverCallStreamObserver;

        private final AtomicBoolean wasReady = new AtomicBoolean(false);

        private OnReadyServerHandler(ServerCallStreamObserver serverCallStreamObserver) {
            this.serverCallStreamObserver = serverCallStreamObserver;
        }

        @Override
        public void run() {
            if (this.serverCallStreamObserver.isReady() && !wasReady.get()) {
                wasReady.set(true);
                log.info("gRPC Greeter service is READY");

                // Signal the request sender to send one message.
                serverCallStreamObserver.request(1);
            }
        }
    }
}
