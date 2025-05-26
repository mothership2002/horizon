package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Convenience annotation for HTTP protocol mappings.
 * 
 * @deprecated Use @ProtocolAccess(schema = @ProtocolSchema(protocol = "HTTP", value = "METHOD /path")) instead
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(HttpResources.class)
@Deprecated
public @interface HttpResource {
    /**
     * HTTP method and path.
     * Examples: "GET /users/{id}", "POST /users", "PUT /users/{id}"
     */
    String value();
    
    /**
     * Content type for this resource.
     */
    String contentType() default "application/json";
    
    /**
     * Whether this is the primary HTTP route.
     */
    boolean primary() default false;
}
