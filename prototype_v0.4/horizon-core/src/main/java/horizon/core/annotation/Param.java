package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Protocol-neutral parameter annotation.
 * Replaces HTTP-specific annotations (@PathParam, @QueryParam, @Header, @RequestBody)
 * with a unified approach that works across all protocols.
 * 
 * The framework will automatically find the parameter value from various sources
 * based on the protocol and naming conventions.
 * 
 * Examples:
 * <pre>
 * // Simple usage - auto-detection
 * @Intent("get")
 * public User getUser(@Param("userId") String userId) {
 *     // Framework will look for userId in:
 *     // - HTTP: path.userId, query.userId, body.userId
 *     // - gRPC: message.userId, message.user_id
 *     // - WebSocket: data.userId, payload.userId
 * }
 * 
 * // With explicit hints
 * @Intent("search")
 * public List<User> searchUsers(
 *     @Param(value = "query", required = false) String searchQuery,
 *     @Param(value = "limit", defaultValue = "10") int limit
 * ) {
 *     // Works across all protocols
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {
    /**
     * The logical name of the parameter.
     * This is protocol-neutral and represents the business meaning.
     */
    String value();
    
    /**
     * Whether this parameter is required.
     * If true and the parameter is not found, an exception will be thrown.
     */
    boolean required() default true;
    
    /**
     * Default value if the parameter is not present.
     * Will be converted to the parameter type.
     */
    String defaultValue() default "";
    
    /**
     * Hints for where to look for this parameter.
     * If empty, the framework will search in all standard locations.
     * 
     * Examples:
     * - "body" - look only in request body
     * - "header" - look only in headers
     * - "path" - look only in path parameters (HTTP)
     * 
     * These are hints, not strict requirements, allowing protocol flexibility.
     */
    String[] hints() default {};
}
