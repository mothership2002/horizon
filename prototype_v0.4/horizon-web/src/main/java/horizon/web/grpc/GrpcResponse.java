package horizon.web.grpc;

/**
 * Simplified gRPC response representation focused on DTO-based communication.
 * 
 * Focuses on essential response data:
 * 1. Success/error status
 * 2. JSON payload for successful responses
 * 3. Error message for failed responses
 */
public class GrpcResponse {
    private final boolean success;
    private final String jsonPayload;
    private final Status status;
    private final String errorMessage;

    private GrpcResponse(boolean success, String jsonPayload, Status status, String errorMessage) {
        this.success = success;
        this.jsonPayload = jsonPayload;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a successful response with JSON payload.
     */
    public static GrpcResponse success(String jsonPayload) {
        return new GrpcResponse(true, jsonPayload, Status.OK, null);
    }

    /**
     * Creates an error response with status and message.
     */
    public static GrpcResponse error(Status status, String errorMessage) {
        return new GrpcResponse(false, null, status, errorMessage);
    }

    /**
     * Creates an error response from an exception.
     */
    public static GrpcResponse error(Throwable throwable) {
        Status status = Status.INTERNAL;
        if (throwable instanceof IllegalArgumentException) {
            status = Status.INVALID_ARGUMENT;
        } else if (throwable instanceof SecurityException) {
            status = Status.PERMISSION_DENIED;
        } else if (throwable.getMessage() != null && throwable.getMessage().toLowerCase().contains("not found")) {
            status = Status.NOT_FOUND;
        }
        
        return error(status, throwable.getMessage());
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getJsonPayload() {
        return jsonPayload;
    }

    public Status getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Simplified gRPC status codes.
     * Only the most commonly used ones for basic error handling.
     */
    public enum Status {
        OK(0),
        CANCELLED(1),
        UNKNOWN(2),
        INVALID_ARGUMENT(3),
        DEADLINE_EXCEEDED(4),
        NOT_FOUND(5),
        ALREADY_EXISTS(6),
        PERMISSION_DENIED(7),
        RESOURCE_EXHAUSTED(8),
        FAILED_PRECONDITION(9),
        ABORTED(10),
        OUT_OF_RANGE(11),
        UNIMPLEMENTED(12),
        INTERNAL(13),
        UNAVAILABLE(14);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    @Override
    public String toString() {
        return "GrpcResponse{" +
                "success=" + success +
                ", jsonPayload='" + (jsonPayload != null ? jsonPayload.substring(0, Math.min(100, jsonPayload.length())) + "..." : "null") + '\'' +
                ", status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
