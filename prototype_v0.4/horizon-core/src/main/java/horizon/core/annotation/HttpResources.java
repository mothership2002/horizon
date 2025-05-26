package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Container for multiple HttpResource annotations.
 * 
 * @deprecated Use @ProtocolAccess with multiple schemas instead
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface HttpResources {
    HttpResource[] value();
}
