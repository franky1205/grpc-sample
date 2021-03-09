package org.franwork.studio.grpc.sample.chatroom;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.franwork.studio.grpc.sample.GrpcConstant;

/**
 * @author Frankie Chao by 2021-03-08.
 */
public class ChatRoomServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String username = headers.get(GrpcConstant.USERNAME_METADATA_KEY);

        Context context = Context.current()
                .withValue(GrpcConstant.USERNAME_CONTEXT_KEY, username);
        return Contexts.interceptCall(context, serverCall, headers, serverCallHandler);
    }
}
