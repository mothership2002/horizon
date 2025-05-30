package horizon.web.grpc;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.grpc.Metadata;
import io.grpc.Status;

/**
 * Represents a gRPC response in the Horizon framework.
 * Can represent both successful responses and errors.
 */
public class GrpcResponse {
    private final Message message;
    private final Status status;
    private final Metadata trailers;
    private final boolean isError;
    private ByteString rawResponseBytes;
    
    private GrpcResponse(Message message, Status status, Metadata trailers, boolean isError) {
        this.message = message;
        this.status = status;
        this.trailers = trailers != null ? trailers : new Metadata();
        this.isError = isError;
    }
    
    /**
     * Creates a successful response.
     */
    public static GrpcResponse success(Message message) {
        return new GrpcResponse(message, Status.OK, new Metadata(), false);
    }
    
    /**
     * Creates a successful response with metadata.
     */
    public static GrpcResponse success(Message message, Metadata trailers) {
        return new GrpcResponse(message, Status.OK, trailers, false);
    }
    
    /**
     * Creates a successful response with raw bytes.
     */
    public static GrpcResponse successWithBytes(ByteString bytes) {
        GrpcResponse response = new GrpcResponse(null, Status.OK, new Metadata(), false);
        response.rawResponseBytes = bytes;
        return response;
    }
    
    /**
     * Creates an error response.
     */
    public static GrpcResponse error(Status status) {
        return new GrpcResponse(null, status, new Metadata(), true);
    }
    
    /**
     * Creates an error response with metadata.
     */
    public static GrpcResponse error(Status status, Metadata trailers) {
        return new GrpcResponse(null, status, trailers, true);
    }
    
    /**
     * Creates an error response from an exception.
     */
    public static GrpcResponse error(Throwable throwable) {
        Status status = switch (throwable) {
            case IllegalArgumentException illegalArgumentException ->
                    Status.INVALID_ARGUMENT.withDescription(throwable.getMessage());
            case SecurityException securityException ->
                    Status.PERMISSION_DENIED.withDescription(throwable.getMessage());
            case UnsupportedOperationException unsupportedOperationException ->
                    Status.UNIMPLEMENTED.withDescription(throwable.getMessage());
            case IllegalStateException illegalStateException ->
                    Status.FAILED_PRECONDITION.withDescription(throwable.getMessage());
            case NullPointerException nullPointerException ->
                    Status.INVALID_ARGUMENT.withDescription("Null value where not permitted");
            default -> {
                if (throwable.getMessage() != null && throwable.getMessage().contains("not found")) {
                    yield Status.NOT_FOUND.withDescription(throwable.getMessage());
                } else {
                    yield Status.INTERNAL.withDescription(throwable.getMessage()).withCause(throwable);
                }
            }
        };
        return error(status);
    }
    
    // Getters
    public Message getMessage() {
        return message;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public Metadata getTrailers() {
        return trailers;
    }
    
    public boolean isError() {
        return isError;
    }
    
    public boolean isSuccess() {
        return !isError;
    }
    
    /**
     * Gets the response as a generic Any message.
     */
    public Any getAsAny() {
        return message != null ? Any.pack(message) : null;
    }
    
    /**
     * Gets the raw response bytes.
     */
    public ByteString getRawResponseBytes() {
        return rawResponseBytes;
    }
    
    /**
     * Sets the raw response bytes.
     */
    public void setRawResponseBytes(ByteString rawResponseBytes) {
        this.rawResponseBytes = rawResponseBytes;
    }
}
