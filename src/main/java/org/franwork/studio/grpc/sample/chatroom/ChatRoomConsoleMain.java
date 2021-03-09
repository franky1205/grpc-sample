package org.franwork.studio.grpc.sample.chatroom;

import com.google.protobuf.Descriptors;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * @author Frankie Chao by 2021-03-05.
 */
@Slf4j
public class ChatRoomConsoleMain {

    public static void main(String[] args) {
        ChatRoomClient chatRoomClient = new ChatRoomClient("localhost", 8686, args[0], serverChatMessage -> {
            if (serverChatMessage.getMessageType() == ChatRoomServiceProto.ServerChatMessage.ChatMessageType.BROCAST) {
                log.info("====> Broadcast message from [{}] : [{}]", serverChatMessage.getFrom(), serverChatMessage.getMessage());
            }
            if (serverChatMessage.getMessageType() == ChatRoomServiceProto.ServerChatMessage.ChatMessageType.UNICAST) {
                log.info("====> Unicast message from [{}] : [{}]", serverChatMessage.getFrom(), serverChatMessage.getMessage());
            }
        });
        Scanner scanner = new Scanner(System.in);
        String nextLine = null;
        while ((nextLine = scanner.nextLine()) != null) {
            if ("exit".equalsIgnoreCase(nextLine)) {
                break;
            }
            if (nextLine.contains(">")) {
                chatRoomClient.sendUnicastMessage(ChatRoomServiceProto.ClientChatMessage.newBuilder()
                        .setDest(nextLine.substring(nextLine.indexOf(">") + 1))
                        .setMessage(nextLine.substring(0, nextLine.indexOf(">")))
                        .build());
            } else {
                chatRoomClient.sendBroadcastMessage(ChatRoomServiceProto.ClientChatMessage.newBuilder()
                        .setMessage(nextLine)
                        .build());
            }
        }
        chatRoomClient.completeWithGrpcServer();
    }
}
