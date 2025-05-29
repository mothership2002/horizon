package horizon.web.grpc.resolver;

import horizon.core.protocol.IntentResolver;
import horizon.web.grpc.GrpcRequest;

/**
 * Intent resolver for gRPC requests.
 * Converts gRPC service/method names to Horizon intents.
 */
public class GrpcIntentResolver implements IntentResolver<GrpcRequest> {
    
    @Override
    public String resolveIntent(GrpcRequest request) {
        // Convert gRPC service/method to intent
        // Example: UserService/CreateUser -> user.create
        String serviceName = request.serviceName();
        String methodName = request.methodName();
        
        // Normalize service name
        if (serviceName.endsWith("Service")) {
            serviceName = serviceName.substring(0, serviceName.length() - 7);
        }
        serviceName = serviceName.toLowerCase();
        
        // Normalize method name
        methodName = normalizeMethodName(methodName);
        
        return serviceName + "." + methodName;
    }
    
    /**
     * Normalizes gRPC method names to Horizon intent format.
     * Examples:
     * - CreateUser -> create
     * - GetUser -> get
     * - UpdateUser -> update
     * - ListUsers -> list
     * - SearchUsers -> search
     */
    private String normalizeMethodName(String methodName) {
        // Handle common patterns
        if (methodName.startsWith("Create")) {
            return "create";
        } else if (methodName.startsWith("Get")) {
            return "get";
        } else if (methodName.startsWith("Update")) {
            return "update";
        } else if (methodName.startsWith("Delete")) {
            return "delete";
        } else if (methodName.startsWith("List")) {
            return "list";
        } else if (methodName.startsWith("Search")) {
            return "search";
        }
        
        // Default: convert CamelCase to lowercase
        return camelToLowerCase(methodName);
    }
    
    /**
     * Converts CamelCase to lowercase.
     */
    private String camelToLowerCase(String camelCase) {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                // Don't add dot before consecutive uppercase letters
                if (i + 1 < camelCase.length() && !Character.isUpperCase(camelCase.charAt(i + 1))) {
                    result.append('.');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }
}
