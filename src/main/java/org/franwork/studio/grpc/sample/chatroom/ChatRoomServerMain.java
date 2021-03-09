package org.franwork.studio.grpc.sample.chatroom;

import java.io.IOException;

/**
 * @author Frankie Chao by 2021-03-08.
 */
public class ChatRoomServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        ChatRoomServer chatRoomServer = new ChatRoomServer(8686);
        chatRoomServer.blockUntilShutdown();
    }
}
