package horizon.web.grpc;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * Represents a gRPC request in the Horizon framework.
 * Wraps gRPC-specific request information.
 */
public class GrpcRequest {
    private final String serviceName;
    private final String methodName;
    private final Message message;
    private final Metadata headers;
    private final MethodDescriptor<?, ?> methodDescriptor;
    private ByteString rawRequestBytes;

    public GrpcRequest(String serviceName, String methodName, Message message, Metadata headers,
                      MethodDescriptor<?, ?> methodDescriptor) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.message = message;
        this.headers = headers;
        this.methodDescriptor = methodDescriptor;
    }

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

    public String serviceName() {
        return serviceName;
    }

    public String methodName() {
        return methodName;
    }

    public Message message() {
        return message;
    }

    public Metadata headers() {
        return headers;
    }

    public MethodDescriptor<?, ?> methodDescriptor() {
        return methodDescriptor;
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
        return message != null ? Any.pack(message) : null;
    }

    /**
     * Gets the raw request bytes.
     */
    public ByteString getRawRequestBytes() {
        return rawRequestBytes;
    }

    /**
     * Sets the raw request bytes.
     */
    public void setRawRequestBytes(ByteString rawRequestBytes) {
        this.rawRequestBytes = rawRequestBytes;
    }
}
