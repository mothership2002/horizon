package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Container annotation for multiple ProtocolMapping annotations.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtocolMappings {
    ProtocolMapping[] value();
}
