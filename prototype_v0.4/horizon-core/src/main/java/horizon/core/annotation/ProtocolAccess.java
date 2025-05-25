package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Specifies which protocols can access this conductor or intent method.
 * If not specified, the conductor is accessible by all protocols (backward compatibility).
 * 
 * Security-first approach: explicitly declare protocol access.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtocolAccess {
    /**
     * List of protocol names that can access this conductor/intent.
     * Examples: "HTTP", "WebSocket", "gRPC", "GraphQL"
     */
    String[] value();
    
    /**
     * Whether to allow access from protocols not explicitly listed.
     * Default is false for security.
     */
    boolean allowOthers() default false;
}
