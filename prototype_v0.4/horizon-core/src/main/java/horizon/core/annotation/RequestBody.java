package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Indicates that a method parameter should be bound to the body of the request.
 * This annotation makes it explicit which parameter receives the request body,
 * especially useful when multiple parameters are present.
 * 
 * @deprecated Since 0.4, use {@link Param} for individual field extraction instead.
 *             Accepting entire request bodies couples code to specific DTOs.
 * 
 * Migration example:
 * <pre>
 * // Old way (DTO coupling):
 * @Intent("create")
 * public User create(@RequestBody CreateUserRequest request) {
 *     return userService.create(request.getName(), request.getEmail());
 * }
 * 
 * // New way (protocol-neutral):
 * @Intent("create")
 * public User create(@Param("name") String name,
 *                   @Param("email") String email) {
 *     return userService.create(name, email);
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated(since = "0.4", forRemoval = true)
public @interface RequestBody {
    /**
     * Whether the body is required.
     * If true and the body is missing, an exception will be thrown.
     */
    boolean required() default true;
}
