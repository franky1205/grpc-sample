package org.franwork.studio.grpc.sample.chatroom;

import io.grpc.ConnectivityState;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.franwork.studio.grpc.sample.AbstractGrpcClient;
import org.franwork.studio.grpc.sample.GrpcConstant;

import java.util.function.Consumer;

/**
 * @author Frankie Chao by 2021-03-05.
 */
@Slf4j
class ChatRoomClient extends AbstractGrpcClient {

    private String username;

    private Consumer<ChatRoomServiceProto.ServerChatMessage> serverChatMessageConsumer;

    private ChatRoomServiceGrpc.ChatRoomServiceStub chatRoomServiceStub;

    private StreamObserver<ChatRoomServiceProto.ClientChatMessage> broadcastObserver;

    private StreamObserver<ChatRoomServiceProto.ClientChatMessage> unicastObserver;

    ChatRoomClient(String serverHost, int port, String username, Consumer<ChatRoomServiceProto.ServerChatMessage> serverChatMessageConsumer) {
        super(serverHost, port);
        this.username = username;
        this.serverChatMessageConsumer = serverChatMessageConsumer;
    }

    @Override
    protected void connectivityStateChanged(ConnectivityState connectivityState) {
        log.info("ConnectivityState changed to [{}] between client and server host.", connectivityState);
        if (ConnectivityState.CONNECTING == connectivityState) {
            this.initMessageStubs();
        }
    }

    private void initMessageStubs() {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstant.USERNAME_METADATA_KEY, this.username);
        this.chatRoomServiceStub = ChatRoomServiceGrpc.newStub(managedChannel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
        this.initBroadcastObserver(this.chatRoomServiceStub);
        this.initUnicastObserver(this.chatRoomServiceStub);
    }

    private void initBroadcastObserver(ChatRoomServiceGrpc.ChatRoomServiceStub chatRoomServiceStub) {
        this.broadcastObserver = chatRoomServiceStub.broadcast(new StreamObserver<ChatRoomServiceProto.ServerChatMessage>() {
            @Override
            public void onNext(ChatRoomServiceProto.ServerChatMessage value) {
                serverChatMessageConsumer.accept(value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Get server error with message: [{}]", t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                log.info("Chat Room Completed !!!");
            }
        });
    }

    private void initUnicastObserver(ChatRoomServiceGrpc.ChatRoomServiceStub chatRoomServiceStub) {
        this.unicastObserver = chatRoomServiceStub.unicast(new StreamObserver<ChatRoomServiceProto.ServerChatMessage>() {
            @Override
            public void onNext(ChatRoomServiceProto.ServerChatMessage value) {
                log.info("Get Unicast Message From [{}] : [{}]", value.getFrom(), value.getMessage());
                serverChatMessageConsumer.accept(value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Get server error with message: [{}]", t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                log.info("Chat Room Completed !!!");
            }
        });
    };

    void sendBroadcastMessage(ChatRoomServiceProto.ClientChatMessage broadcastMessage) {
        this.broadcastObserver.onNext(broadcastMessage);
    }

    void sendUnicastMessage(ChatRoomServiceProto.ClientChatMessage unicastMessage) {
        this.unicastObserver.onNext(unicastMessage);
    }

    void completeWithGrpcServer() {
        this.broadcastObserver.onCompleted();
        this.unicastObserver.onCompleted();
    }
}
