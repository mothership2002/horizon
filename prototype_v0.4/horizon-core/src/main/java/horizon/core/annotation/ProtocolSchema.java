package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Defines a protocol-specific schema for mapping.
 * This is used within @ProtocolAccess to specify how each protocol accesses the method.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtocolSchema {
    /**
     * The protocol name (e.g., "HTTP", "WebSocket", "gRPC").
     * Use constants from ProtocolNames for built-in protocols.
     */
    String protocol();
    
    /**
     * The protocol-specific schema string.
     * Format depends on the protocol:
     * - HTTP: "METHOD /path" (e.g., "GET /users/{id}", "POST /users")
     * - WebSocket: "event.name" (e.g., "user.create", "chat.message")
     * - gRPC: "Service.Method" (e.g., "UserService.CreateUser")
     * 
     * If not specified, the framework will use convention-based mapping.
     */
    String value() default "";
    
    /**
     * Additional attributes for this schema.
     * Key-value pairs for protocol-specific configuration.
     */
    String[] attributes() default {};
}
