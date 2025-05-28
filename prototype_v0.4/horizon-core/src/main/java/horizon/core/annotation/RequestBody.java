package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Indicates that a method parameter should be bound to the body of the request.
 * This annotation makes it explicit which parameter receives the request body,
 * especially useful when multiple parameters are present.
 * 
 * Example:
 * <pre>
 * @Intent("update")
 * public Result updateUser(
 *     @PathParam("id") Long userId,
 *     @QueryParam("notify") Boolean notify,
 *     @RequestBody UpdateUserRequest request
 * ) {
 *     // request parameter will receive the request body
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBody {
    /**
     * Whether the body is required.
     * If true and the body is missing, an exception will be thrown.
     */
    boolean required() default true;
}
