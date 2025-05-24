package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Marks a method as an intent handler within a Conductor.
 * The method will be invoked when the specified intent is received.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Intent {
    /**
     * The intent name (e.g., "create", "get", "delete").
     * This will be combined with the conductor's namespace to form the full intent.
     */
    String value();
    
    /**
     * Alternative intent patterns that also map to this method.
     */
    String[] aliases() default {};
    
    /**
     * HTTP method constraints. If specified, this intent will only match
     * for the specified HTTP methods. Empty means all methods are allowed.
     */
    String[] httpMethods() default {};
    
    /**
     * HTTP path pattern for more specific matching.
     * Can include placeholders like {id}.
     * Example: "/users/{id}/orders"
     */
    String httpPath() default "";
}
