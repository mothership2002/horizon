package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Binds a method parameter to an HTTP header value.
 * 
 * Example:
 * <pre>
 * @Intent("upload")
 * public Result upload(@Header("Content-Type") String contentType,
 *                     @Header("X-Auth-Token") String authToken) {
 *     // Header values will be injected
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
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
