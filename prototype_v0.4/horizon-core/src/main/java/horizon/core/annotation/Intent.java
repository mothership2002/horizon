package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Marks a method as an intent handler within a Conductor.
 * The method will be invoked when the specified intent is received.
 * 
 * This annotation is protocol-neutral. Use @ProtocolAccess for protocol-specific configurations.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Intent {
    /**
     * The intent name (e.g., "create", "get", "delete").
     * This will be combined with the conductor's namespace to form the full intent.
     */
    String value();
    
    /**
     * Alternative intent patterns that also map to this method.
     */
    String[] aliases() default {};
}
