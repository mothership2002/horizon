package horizon.web.grpc.resolver;

import horizon.core.protocol.IntentResolver;
import horizon.core.util.NamingUtils;
import horizon.web.grpc.GrpcRequest;

/**
 * Intent resolver for gRPC requests.
 * Converts gRPC service/method names to Horizon intents.
 * 
 * Conversion examples:
 * - UserService/CreateUser -> user.create
 * - OrderService/GetOrderById -> order.get
 * - ProductService/SearchProducts -> product.search
 */
public class GrpcIntentResolver implements IntentResolver<GrpcRequest> {
    
    @Override
    public String resolveIntent(GrpcRequest request) {
        String serviceName = normalizeServiceName(request.serviceName());
        String methodName = normalizeMethodName(request.methodName());
        
        return serviceName + "." + methodName;
    }
    
    /**
     * Normalizes the service name by removing common suffixes and converting to lowercase.
     * 
     * @param serviceName the gRPC service name
     * @return the normalized service name
     */
    private String normalizeServiceName(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            return serviceName;
        }
        
        // Remove common service suffixes
        String normalized = serviceName;
        if (normalized.endsWith("Service")) {
            normalized = normalized.substring(0, normalized.length() - 7);
        } else if (normalized.endsWith("Svc")) {
            normalized = normalized.substring(0, normalized.length() - 3);
        }
        
        return normalized.toLowerCase();
    }
    
    /**
     * Normalizes gRPC method names to Horizon intent format.
     * Uses NamingUtils for consistent naming conventions.
     * 
     * @param methodName the gRPC method name
     * @return the normalized method name
     */
    private String normalizeMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            return methodName;
        }
        
        // First try to extract common action patterns
        String action = NamingUtils.extractAction(methodName);
        
        // If it's a simple action (create, get, update, etc.), use it directly
        if (isSimpleAction(action)) {
            return action;
        }
        
        // Otherwise, convert the full method name
        return NamingUtils.camelCaseToDotNotation(methodName);
    }
    
    /**
     * Checks if the action is a simple CRUD operation.
     * 
     * @param action the action to check
     * @return true if it's a simple action
     */
    private boolean isSimpleAction(String action) {
        return action.matches("create|get|update|delete|list|search|find|save|remove");
    }
}
