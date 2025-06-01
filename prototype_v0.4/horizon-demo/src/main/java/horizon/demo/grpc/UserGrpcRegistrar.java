package horizon.demo.grpc;

import horizon.demo.model.*;
import horizon.web.grpc.GrpcServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the message types for each gRPC method in the UserService.
 * This ensures proper integration between the UserConductor and the gRPC service.
 */
public class UserGrpcRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(UserGrpcRegistrar.class);
    
    /**
     * Registers all message types for the UserService.
     * This method should be called during application startup.
     */
    public static void registerMessageTypes() {
        logger.info("Registering message types for UserService");
        
        GrpcServiceRegistry registry = GrpcServiceRegistry.getInstance();
        
        // Register CreateUser message types
        registry.registerMessageTypes(
                "UserService/CreateUser",
                CreateUserRequest.class,
                CreateUserResponse.class
        );
        
        // Register GetUser message types
        registry.registerMessageTypes(
                "UserService/GetUser",
                GetUserRequest.class,
                GetUserResponse.class
        );
        
        // Register UpdateUser message types
        registry.registerMessageTypes(
                "UserService/UpdateUser",
                UpdateUserRequest.class,
                UpdateUserResponse.class
        );
        
        // Register DeleteUser message types
        registry.registerMessageTypes(
                "UserService/DeleteUser",
                DeleteUserRequest.class,
                DeleteUserResponse.class
        );
        
        // Register ListUsers message types
        registry.registerMessageTypes(
                "UserService/ListUsers",
                ListUsersRequest.class,
                ListUsersResponse.class
        );
        
        // Register ValidateUser message types
        registry.registerMessageTypes(
                "UserService/ValidateUser",
                ValidateUserRequest.class,
                ValidateUserResponse.class
        );
        
        logger.info("Message types registered successfully for UserService");
    }
}