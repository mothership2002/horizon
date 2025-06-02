package horizon.web.grpc;

import java.util.HashMap;
import java.util.Map;

/**
 * Simplified gRPC request representation focused on DTO-based communication.
 * 
 * This class focuses on the essential data needed for processing:
 * 1. Service and method identification
 * 2. JSON payload (converted from protobuf)
 * 3. Metadata (headers)
 * 
 * No complex protobuf handling - everything is JSON-based for simplicity.
 */
public class GrpcRequest {
    private final String serviceName;
    private final String methodName;
    private final String jsonPayload;
    private final Map<String, String> metadata;

    public GrpcRequest(String serviceName, String methodName, String jsonPayload) {
        this(serviceName, methodName, jsonPayload, new HashMap<>());
    }

    public GrpcRequest(String serviceName, String methodName, String jsonPayload, Map<String, String> metadata) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.jsonPayload = jsonPayload;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    /**
     * Creates a gRPC request from a full method name.
     * 
     * @param fullMethodName format: "ServiceName/MethodName"
     * @param jsonPayload the JSON payload
     * @return the gRPC request
     */
    public static GrpcRequest fromFullMethodName(String fullMethodName, String jsonPayload) {
        String[] parts = fullMethodName.split("/", 2);
        String serviceName = parts.length > 0 ? parts[0] : "";
        String methodName = parts.length > 1 ? parts[1] : "";
        
        return new GrpcRequest(serviceName, methodName, jsonPayload);
    }

    /**
     * Creates a gRPC request with metadata.
     */
    public static GrpcRequest withMetadata(String serviceName, String methodName, 
                                          String jsonPayload, Map<String, String> metadata) {
        return new GrpcRequest(serviceName, methodName, jsonPayload, metadata);
    }

    // Getters
    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getJsonPayload() {
        return jsonPayload;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getFullMethodName() {
        return serviceName + "/" + methodName;
    }

    // Legacy compatibility methods for existing code
    public String serviceName() {
        return serviceName;
    }

    public String methodName() {
        return methodName;
    }

    // Utility methods
    public String getMetadata(String key) {
        return metadata.get(key);
    }

    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    @Override
    public String toString() {
        return "GrpcRequest{" +
                "serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", jsonPayload='" + (jsonPayload != null ? jsonPayload.substring(0, Math.min(100, jsonPayload.length())) + "..." : "null") + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
