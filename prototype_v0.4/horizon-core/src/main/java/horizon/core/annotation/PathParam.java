package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Binds a method parameter to a path variable.
 * Similar to JAX-RS @PathParam or Spring's @PathVariable.
 * 
 * @deprecated Since 0.4, use {@link Param} instead for protocol-neutral parameter binding.
 *             This annotation only works with HTTP protocol.
 * 
 * Migration example:
 * <pre>
 * // Old way (HTTP-only):
 * @Intent("get")
 * public User getUser(@PathParam("id") String userId) { }
 * 
 * // New way (all protocols):
 * @Intent("get")
 * public User getUser(@Param("userId") String userId) { }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated(since = "0.4", forRemoval = true)
public @interface PathParam {
    /**
     * The name of the path parameter.
     */
    String value();
}
