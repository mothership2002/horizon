package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Convenience annotation for WebSocket protocol mappings.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(WebSocketResources.class)
public @interface WebSocketResource {
    /**
     * WebSocket event name or pattern.
     * Examples: "user.create", "chat.message", "order.*"
     */
    String value();
    
    /**
     * Whether this is a streaming/subscription endpoint.
     */
    boolean streaming() default false;
    
    /**
     * Whether this is the primary WebSocket route.
     */
    boolean primary() default false;
}
