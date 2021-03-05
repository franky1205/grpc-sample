package org.franwork.studio.grpc.sample.routeguide;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * @author Frankie Chao by 2021-03-03.
 */
public class RouteGudeServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        Context context = Context.current();
//        return Contexts.interceptCall();
        return Contexts.interceptCall(context, call, headers, next);
    }
}
