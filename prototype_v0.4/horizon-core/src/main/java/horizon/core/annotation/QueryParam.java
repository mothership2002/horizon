package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Binds a method parameter to a query string parameter.
 * Similar to JAX-RS @QueryParam or Spring's @RequestParam.
 * 
 * @deprecated Since 0.4, use {@link Param} instead for protocol-neutral parameter binding.
 *             This annotation only works with HTTP protocol.
 * 
 * Migration example:
 * <pre>
 * // Old way (HTTP-only):
 * @Intent("search")
 * public List<User> search(@QueryParam("q") String query,
 *                         @QueryParam("limit") int limit) { }
 * 
 * // New way (all protocols):
 * @Intent("search")
 * public List<User> search(@Param("query") String query,
 *                         @Param(value = "limit", defaultValue = "10") int limit) { }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated(since = "0.4", forRemoval = true)
public @interface QueryParam {
    /**
     * The name of the query parameter.
     */
    String value();
    
    /**
     * Whether the parameter is required.
     */
    boolean required() default true;
    
    /**
     * Default value if the parameter is not present.
     */
    String defaultValue() default "";
}
