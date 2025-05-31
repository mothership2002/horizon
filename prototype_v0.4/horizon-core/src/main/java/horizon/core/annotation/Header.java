package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Binds a method parameter to an HTTP header value.
 * 
 * @deprecated Since 0.4, use {@link Param} with hints instead for protocol-neutral parameter binding.
 *             This annotation is HTTP-specific.
 * 
 * Migration example:
 * <pre>
 * // Old way (HTTP-only):
 * @Intent("secure")
 * public Result secure(@Header("Authorization") String auth) { }
 * 
 * // New way (all protocols):
 * @Intent("secure")
 * public Result secure(@Param(value = "authorization", hints = {"header"}) String auth) { }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated(since = "0.4", forRemoval = true)
public @interface Header {
    /**
     * The name of the header.
     */
    String value();
    
    /**
     * Whether the header is required.
     */
    boolean required() default false;
}
