package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Binds a method parameter to a path variable.
 * Similar to JAX-RS @PathParam or Spring's @PathVariable.
 * 
 * Example:
 * <pre>
 * @Intent("get")
 * public User getUser(@PathParam("id") Long userId) {
 *     // userId will be extracted from path like /users/123
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathParam {
    /**
     * The name of the path parameter.
     */
    String value();
}
