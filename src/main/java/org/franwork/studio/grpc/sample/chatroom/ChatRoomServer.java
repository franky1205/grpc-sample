package org.franwork.studio.grpc.sample.chatroom;

import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import org.franwork.studio.grpc.sample.AbstractGrpcServer;

import java.io.IOException;

/**
 * @author Frankie Chao by 2021-03-05.
 */
public class ChatRoomServer extends AbstractGrpcServer {

    protected ChatRoomServer(int serverPort) throws IOException {
        super(ServerBuilder.forPort(serverPort)
                .addService(ServerInterceptors.intercept(new ChatRoomServerService(), new ChatRoomServerInterceptor()))
                .build()
                .start());
    }
}
