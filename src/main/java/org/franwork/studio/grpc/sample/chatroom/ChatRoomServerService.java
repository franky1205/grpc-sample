package org.franwork.studio.grpc.sample.chatroom;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.franwork.studio.grpc.sample.GrpcConstant;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Frankie Chao by 2021-03-05.
 */
@Slf4j
public class ChatRoomServerService extends ChatRoomServiceGrpc.ChatRoomServiceImplBase {

    private Set<StreamObserver<ChatRoomServiceProto.ServerChatMessage>> broadcastObservers = new CopyOnWriteArraySet<>();

    private Map<String, StreamObserver<ChatRoomServiceProto.ServerChatMessage>> unicastObservers = new ConcurrentHashMap<>();

    @Override
    public StreamObserver<ChatRoomServiceProto.ClientChatMessage> broadcast(StreamObserver<ChatRoomServiceProto.ServerChatMessage> responseObserver) {
        this.broadcastObservers.add(responseObserver);

        return new StreamObserver<ChatRoomServiceProto.ClientChatMessage>() {
            @Override
            public void onNext(ChatRoomServiceProto.ClientChatMessage value) {
                log.info("Get broadcast client chat message from: [{}]", GrpcConstant.USERNAME_CONTEXT_KEY.get());
                broadcastObservers.forEach(broadcastObserver -> broadcastObserver.onNext(ChatRoomServiceProto.ServerChatMessage.newBuilder()
                                .setMessageType(ChatRoomServiceProto.ServerChatMessage.ChatMessageType.BROCAST)
                                .setFrom(GrpcConstant.USERNAME_CONTEXT_KEY.get())
                                .setMessage(value.getMessage())
                                .build()));
            }

            @Override
            public void onError(Throwable t) {
                broadcastObservers.remove(responseObserver);
                log.error("gRPC broadcast with error: [{}]", t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                broadcastObservers.remove(responseObserver);
                log.info("gRPC broadcast client completed.");
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<ChatRoomServiceProto.ClientChatMessage> unicast(StreamObserver<ChatRoomServiceProto.ServerChatMessage> responseObserver) {
        String username = GrpcConstant.USERNAME_CONTEXT_KEY.get();
        this.unicastObservers.put(username, responseObserver);
        return new StreamObserver<ChatRoomServiceProto.ClientChatMessage>() {
            @Override
            public void onNext(ChatRoomServiceProto.ClientChatMessage value) {
                log.info("Get unicast client chat message from: [{}]", GrpcConstant.USERNAME_CONTEXT_KEY.get());
                if (!unicastObservers.containsKey(value.getDest())) {
                    responseObserver.onNext(ChatRoomServiceProto.ServerChatMessage.newBuilder()
                            .setMessageType(ChatRoomServiceProto.ServerChatMessage.ChatMessageType.ERROR)
                            .setFrom("Chat Room Server")
                            .setMessage("Cannot find the login user with name: [" + value.getDest() + "]")
                            .build());
                    return;
                }

                Optional.ofNullable(unicastObservers.get(value.getDest()))
                        .ifPresent(unicastObserver -> unicastObserver.onNext(ChatRoomServiceProto.ServerChatMessage.newBuilder()
                                .setMessageType(ChatRoomServiceProto.ServerChatMessage.ChatMessageType.UNICAST)
                                .setFrom(GrpcConstant.USERNAME_CONTEXT_KEY.get())
                                .setMessage(value.getMessage())
                                .build()));
            }

            @Override
            public void onError(Throwable t) {
                unicastObservers.remove(GrpcConstant.USERNAME_CONTEXT_KEY.get());
                log.error("gRPC unicast of user: [{}] with error: [{}]", GrpcConstant.USERNAME_CONTEXT_KEY.get(), t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                unicastObservers.remove(GrpcConstant.USERNAME_CONTEXT_KEY.get());
                log.info("gRPC unicast client: [{}] completed.", GrpcConstant.USERNAME_CONTEXT_KEY.get());
            }
        };
    }
}
