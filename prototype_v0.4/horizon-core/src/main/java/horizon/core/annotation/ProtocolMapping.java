package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Maps an intent method to specific protocol(s) with their routing information.
 * 
 * @deprecated Use @ProtocolAccess(schema = @ProtocolSchema(...)) instead for better integration
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(ProtocolMappings.class)
@Deprecated
public @interface ProtocolMapping {
    /**
     * The protocol this mapping applies to.
     * Use constants from ProtocolNames (e.g., ProtocolNames.HTTP) or custom protocol names.
     */
    String protocol();
    
    /**
     * Protocol-specific mapping strings.
     * Examples:
     * - HTTP: "GET /users/{id}", "POST /users"
     * - WebSocket: "user.create", "user.update"
     * - gRPC: "UserService.CreateUser"
     * - GraphQL: "mutation createUser"
     */
    String[] mapping();
    
    /**
     * Additional protocol-specific attributes as key-value pairs.
     * Examples:
     * - {"contentType", "application/json"}
     * - {"streaming", "true"}
     * - {"timeout", "30000"}
     */
    String[] attributes() default {};
    
    /**
     * Whether this mapping should be used as the primary/default route.
     */
    boolean primary() default false;
}
