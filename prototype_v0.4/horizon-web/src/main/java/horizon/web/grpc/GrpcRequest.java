package horizon.web.grpc;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * Represents a gRPC request in the Horizon framework.
 * Wraps gRPC-specific request information.
 */
public record GrpcRequest(String serviceName, String methodName, Message message, Metadata headers,
                          MethodDescriptor<?, ?> methodDescriptor) {

    /**
     * Creates a gRPC request from a service call.
     */
    public static GrpcRequest of(String fullMethodName, Message message, Metadata headers,
                                 MethodDescriptor<?, ?> methodDescriptor) {
        String[] parts = fullMethodName.split("/");
        String serviceName = parts.length > 1 ? parts[0] : "";
        String methodName = parts.length > 1 ? parts[1] : fullMethodName;

        return new GrpcRequest(serviceName, methodName, message, headers, methodDescriptor);
    }

    public String getFullMethodName() {
        return serviceName + "/" + methodName;
    }

    /**
     * Checks if this is a streaming request.
     */
    public boolean isStreaming() {
        return methodDescriptor != null &&
                (methodDescriptor.getType() == MethodDescriptor.MethodType.CLIENT_STREAMING ||
                        methodDescriptor.getType() == MethodDescriptor.MethodType.BIDI_STREAMING);
    }

    /**
     * Gets the request as a generic Any message.
     */
    public Any getAsAny() {
        return Any.pack(message);
    }
}
