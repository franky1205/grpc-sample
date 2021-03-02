package org.franwork.studio.grpc.sample.helloworld;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.Channel;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.franwork.studio.grpc.sample.helloworld.HelloWorldProto.HelloReply;
import org.franwork.studio.grpc.sample.helloworld.HelloWorldProto.HelloRequest;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * The implementation of HelloWorldClient is original
 *
 * @author Frankie Chao by 2020-05-27.
 */
@Slf4j
public class HelloWorldClient {

    private final static List<String> names = Arrays.asList(
            "Sophia",
            "Jackson",
            "Emma",
            "Aiden",
            "Olivia",
            "Lucas",
            "Ava",
            "Liam",
            "Mia",
            "Noah",
            "Isabella",
            "Ethan",
            "Riley",
            "Mason",
            "Aria",
            "Caden",
            "Zoe",
            "Oliver",
            "Charlotte",
            "Elijah",
            "Lily",
            "Grayson",
            "Layla",
            "Jacob",
            "Amelia",
            "Michael",
            "Emily",
            "Benjamin",
            "Madelyn",
            "Carter",
            "Aubrey",
            "James",
            "Adalyn",
            "Jayden",
            "Madison",
            "Logan",
            "Chloe",
            "Alexander",
            "Harper",
            "Caleb",
            "Abigail",
            "Ryan",
            "Aaliyah",
            "Luke",
            "Avery",
            "Daniel",
            "Evelyn",
            "Jack",
            "Kaylee",
            "William",
            "Ella",
            "Owen",
            "Ellie",
            "Gabriel",
            "Scarlett",
            "Matthew",
            "Arianna",
            "Connor",
            "Hailey",
            "Jayce",
            "Nora",
            "Isaac",
            "Addison",
            "Sebastian",
            "Brooklyn",
            "Henry",
            "Hannah",
            "Muhammad",
            "Mila",
            "Cameron",
            "Leah",
            "Wyatt",
            "Elizabeth",
            "Dylan",
            "Sarah",
            "Nathan",
            "Eliana",
            "Nicholas",
            "Mackenzie",
            "Julian",
            "Peyton",
            "Eli",
            "Maria",
            "Levi",
            "Grace",
            "Isaiah",
            "Adeline",
            "Landon",
            "Elena",
            "David",
            "Anna",
            "Christian",
            "Victoria",
            "Andrew",
            "Camilla",
            "Brayden",
            "Lillian",
            "John",
            "Natalie",
            "Lincoln"
    );

    private static final CountDownLatch done = new CountDownLatch(3);

    /**
     * Blocking-style stub that supports "unary and streaming" output calls on the service
     */
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /**
     * ListenableFuture-style stub that supports "unary calls" on the service
     */
    private final GreeterGrpc.GreeterFutureStub futureStub;

    /**
     * Async stub that supports all call types for the service
     */
    private final GreeterGrpc.GreeterStub greeterStub;

    HelloWorldClient(Channel channel) {
        blockingStub = GreeterGrpc.newBlockingStub(channel);
        futureStub = GreeterGrpc.newFutureStub(channel);
        greeterStub = GreeterGrpc.newStub(channel);
    }

    private void sayBlockingGreet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        try {
            final long startTimeMillis = System.currentTimeMillis();
            HelloReply reply = blockingStub.sayHello(request);
            System.out.println(Thread.currentThread().getName() + "-Get reply message from server: " + reply.getMessage() +
                    " in " + (System.currentTimeMillis() - startTimeMillis) + " millis.");
        } catch (Exception ex) {
            log.error("Get returns from HelloReply with error: [{}]", ex.getMessage(), ex);
        } finally {
            done.countDown();
        }
    }

    private void sayFutureGreet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        try {
            final long startTimeMillis = System.currentTimeMillis();
            ListenableFuture<HelloReply> futureHelloReply = futureStub.sayHello(request);
            Futures.addCallback(futureHelloReply, new FutureCallback<HelloReply>() {
                @Override
                public void onSuccess(@NullableDecl HelloReply result) {
                    log.info(Thread.currentThread().getName() + "- Get reply message from server by future: " +
                            result.getMessage() + " in " + (System.currentTimeMillis() - startTimeMillis) + " millis.");
                    done.countDown();
                }

                @Override
                public void onFailure(Throwable t) {
                    log.error("Get returns from HelloReply with error: [{}]", t.getMessage(), t);
                    done.countDown();
                }
            }, ForkJoinPool.commonPool());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sayHelloStreaming() {
        this.greeterStub.sayHelloStreaming(new GreeterClientResponseObserver());
    }

    private final static ManagedChannel managedChannel = ManagedChannelBuilder
            .forAddress("localhost", 8888)
            .usePlaintext()
            .build();

    public static void main(String[] args) throws Exception {
        ConnectivityState connectivityState = managedChannel.getState(true);
        log.info("The connectivity state between client and server: " + connectivityState);
        managedChannel.notifyWhenStateChanged(connectivityState, HelloWorldClient::sendMessages);

        done.await();

        shutdownQuietly();
    }

    private static void sendMessages() {
        try {
            HelloWorldClient helloWorldClient = new HelloWorldClient(managedChannel);
            helloWorldClient.sayFutureGreet("Frankie-Future");
            Thread.sleep(15000);
            helloWorldClient.sayBlockingGreet("Frankie-Block");
            helloWorldClient.sayHelloStreaming();
        } catch (Exception e) {
            log.error("Say Hello with error: [{}]", e.getMessage(), e);
        } finally {
            shutdownQuietly();
        }
        System.out.println("HelloWorldClient has been shutdown successfully.");
    }

    private static void shutdownQuietly() {
        try {
            managedChannel.shutdown();
            managedChannel.awaitTermination(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Shutdown with error: [{}]", e.getMessage(), e);
        }
    }

    // When using manual flow-control and back-pressure on the client, the ClientResponseObserver handles both request and response streams.
    private static class GreeterClientResponseObserver implements ClientResponseObserver<HelloRequest, HelloReply> {

        private ClientCallStreamObserver<HelloRequest> requestStream;

        @Override
        public void beforeStart(ClientCallStreamObserver<HelloRequest> requestStream) {
            this.requestStream = requestStream;
            // Set up manual flow control for the response stream. It feels backwards to configure the response
            // stream's flow control using the request stream's observer, but this is the way it is.
            requestStream.disableAutoInboundFlowControl();

            // Set up a back-pressure-aware producer for the request stream. The onReadyHandler will be invoked
            // when the consuming side has enough buffer space to receive more messages.
            //
            // Messages are serialized into a transport-specific transmit buffer. Depending on the size of this buffer,
            // MANY messages may be buffered, however, they haven't yet been sent to the server. The server must call
            // request() to pull a buffered message from the client.
            //
            // Note: the onReadyHandler's invocation is serialized on the same thread pool as the incoming
            // StreamObserver's onNext(), onError(), and onComplete() handlers. Blocking the onReadyHandler will prevent
            // additional messages from being processed by the incoming StreamObserver. The onReadyHandler must return
            // in a timely manner or else message processing throughput will suffer.
            requestStream.setOnReadyHandler(new OnReadyClientHandler(requestStream, names.iterator()));
        }

        @Override
        public void onNext(HelloReply value) {
            log.info("<-- Receive message from Server: [{}]", value.getMessage());
            // Signal the sender to send one message.
            requestStream.request(1);
        }

        @Override
        public void onError(Throwable t) {
            log.error("Error found by GreeterClientResponseObserver: [{}]", t.getMessage(), t);
            done.countDown();
        }

        @Override
        public void onCompleted() {
            log.info("All Done");
            done.countDown();
        }
    }

    // Set up a back-pressure-aware producer for the request stream. The onReadyClientHandler will be invoked
    // when the consuming side has enough buffer space to receive more messages.
    //
    // Messages are serialized into a transport-specific transmit buffer. Depending on the size of this buffer,
    // MANY messages may be buffered, however, they haven't yet been sent to the server. The server must call
    // request() to pull a buffered message from the client.
    //
    // Note: the onReadyHandler's invocation is serialized on the same thread pool as the incoming
    // StreamObserver's onNext(), onError(), and onComplete() handlers. Blocking the onReadyHandler will prevent
    // additional messages from being processed by the incoming StreamObserver. The onReadyHandler must return
    // in a timely manner or else message processing throughput will suffer.
    private static class OnReadyClientHandler implements Runnable {

        private final ClientCallStreamObserver<HelloRequest> requestStream;

        private Iterator<String> names;

        private OnReadyClientHandler(ClientCallStreamObserver<HelloRequest> requestStream, Iterator<String> names) {
            this.requestStream = requestStream;
            this.names = names;
        }

        @Override
        public void run() {
            // Start generating values from where we left off on a non-gRPC thread.
            while (requestStream.isReady()) {
                if (names.hasNext()) {
                    // Send more messages if there are more messages to send.
                    String name = names.next();
                    log.info("--> Send request name: [{}]", name);
                    HelloRequest request = HelloRequest.newBuilder().setName(name).build();
                    requestStream.onNext(request);
                } else {
                    requestStream.onCompleted();
                }
            }
        }

    }
}
