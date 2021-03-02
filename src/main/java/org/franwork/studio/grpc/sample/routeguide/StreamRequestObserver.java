package org.franwork.studio.grpc.sample.routeguide;

import com.google.common.base.Preconditions;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

/**
 * StreamRequestObserver is a public abstract class who is initialized with a given response StreamObserver.
 *
 * @author Frankie Chao by 2020-06-01.
 */
@Slf4j
public abstract class StreamRequestObserver<Req, Res> implements StreamObserver<Req> {

    private final ServerCallStreamObserver<Res> responseStreamObserver;

    /**
     * Public constructor of ServerStreamObserver which accept a response StreamObserver of type ServerCallStreamObserver.
     *
     * @param responseStreamObserver the StreamObserver object which should be an instance of ServerCallStreamObserver.
     */
    public StreamRequestObserver(StreamObserver<Res> responseStreamObserver) {
        Preconditions.checkArgument(responseStreamObserver instanceof ServerCallStreamObserver,
                "Only allow the type of ServerCallStreamObserver to be expected response StreamObserver. " +
                        "Type of incoming response StreamObserver: " + responseStreamObserver.getClass());
        this.responseStreamObserver = (ServerCallStreamObserver<Res>) responseStreamObserver;
        this.responseStreamObserver.setOnReadyHandler(() -> this.responseStreamObserver.request(1));
    }

    @Override
    public void onNext(Req request) {
        try {
            log.debug("--> Receive request with type: [{}]", request.getClass());
            this.responseStreamObserver.onNext(this.getResponse(request));

            if (this.responseStreamObserver.isReady()) {
                this.responseStreamObserver.request(1);
            }
        } catch (Throwable ex) {
            log.error("Process request with error: [{}]", ex.getMessage(), ex);
            this.responseStreamObserver.onError(ex);
        }
    }

    @Override
    public void onError(Throwable t) {
        log.error("Client present an error: [{}]. Complete the response stream.", t.getMessage(), t);
        this.responseStreamObserver.onCompleted();
    }

    @Override
    public void onCompleted() {
        log.info("Client completes the request stream.");
        this.responseStreamObserver.onCompleted();
    }

    protected abstract Res getResponse(Req request);
}
