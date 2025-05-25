package horizon.core.annotation;

import horizon.core.protocol.Protocol;
import java.lang.annotation.*;

/**
 * Maps an intent method to specific protocol(s) with their routing information.
 * Having a @ProtocolMapping automatically grants access to that protocol.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(ProtocolMappings.class)
public @interface ProtocolMapping {
    /**
     * The protocol this mapping applies to.
     * Can be either a BuiltInProtocols enum name or a custom protocol name.
     */
    String protocol();
    
    /**
     * Protocol-specific resource identifiers.
     * Examples:
     * - HTTP: "GET /users/{id}", "POST /users"
     * - WebSocket: "user.create", "user.update"
     * - gRPC: "UserService.CreateUser"
     * - GraphQL: "mutation createUser"
     */
    String[] resources();
    
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
