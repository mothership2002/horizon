package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Convenience annotation for HTTP protocol mappings.
 * This is syntactic sugar over @ProtocolMapping(protocol = "HTTP").
 * 
 * @deprecated Use @ProtocolMapping instead for protocol neutrality
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface HttpMapping {
    /**
     * HTTP methods that trigger this intent (GET, POST, PUT, DELETE, etc.).
     */
    String[] methods() default {};
    
    /**
     * Path pattern for matching HTTP requests.
     */
    String path() default "";
}
