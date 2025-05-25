package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Container for multiple HttpResource annotations.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpResources {
    HttpResource[] value();
}
