package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Provides HTTP-specific mapping information for an intent method.
 * This annotation allows fine-grained control over HTTP routing.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpMapping {
    /**
     * HTTP methods that trigger this intent (GET, POST, PUT, DELETE, etc.).
     * Empty array means all methods are accepted.
     */
    String[] methods() default {};
    
    /**
     * Path pattern for matching HTTP requests.
     * Supports path variables: /users/{id}
     * Supports wildcards: /users/*
     */
    String path() default "";
    
    /**
     * Whether this mapping should take priority over default intent mapping.
     */
    boolean priority() default true;
}
