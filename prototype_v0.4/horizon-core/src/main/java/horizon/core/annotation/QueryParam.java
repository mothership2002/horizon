package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Binds a method parameter to a query string parameter.
 * Similar to JAX-RS @QueryParam or Spring's @RequestParam.
 * 
 * Example:
 * <pre>
 * @Intent("search")
 * public List<User> searchUsers(@QueryParam("q") String query,
 *                               @QueryParam("limit") Integer limit) {
 *     // query and limit will be extracted from ?q=john&limit=10
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
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
