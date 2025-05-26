package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Defines protocol access and mapping for a conductor or method.
 * This annotation combines access control with protocol-specific routing.
 * 
 * Examples:
 * <pre>
 * // Simple protocol access (uses convention-based routing)
 * @ProtocolAccess({"HTTP", "WebSocket"})
 * 
 * // With explicit schema mapping
 * @ProtocolAccess(
 *     schema = {
 *         @ProtocolSchema(protocol = "HTTP", value = "POST /users"),
 *         @ProtocolSchema(protocol = "WebSocket", value = "user.create")
 *     }
 * )
 * 
 * // HTTP-only with custom path
 * @ProtocolAccess(
 *     schema = @ProtocolSchema(protocol = "HTTP", value = "GET /api/v2/users/{id}")
 * )
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtocolAccess {
    /**
     * Simple list of allowed protocols (when not using schema).
     * This is a shorthand for basic access control without custom routing.
     * Example: @ProtocolAccess({"HTTP", "WebSocket"})
     */
    String[] value() default {};
    
    /**
     * Protocol schemas that define how each protocol accesses this resource.
     * If a protocol has a schema defined, it automatically has access.
     * Takes precedence over the value() attribute.
     */
    ProtocolSchema[] schema() default {};
    
    /**
     * Whether to allow access from protocols not explicitly listed.
     * Default is false for security.
     * Only applies when using value() attribute, not schema.
     */
    boolean allowOthers() default false;
}
