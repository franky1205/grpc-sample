package org.franwork.studio.grpc.sample;

import io.grpc.Context;
import io.grpc.Metadata;

/**
 * @author Frankie Chao by 2021-03-08.
 */
public interface GrpcConstant {

    Metadata.Key<String> USERNAME_METADATA_KEY = Metadata.Key.of("username", Metadata.ASCII_STRING_MARSHALLER);

    Context.Key<String> USERNAME_CONTEXT_KEY = Context.key("username");
}
