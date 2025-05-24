package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Marks a class as a Conductor that handles specific intents.
 * The class will be automatically registered with the ProtocolAggregator.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conductor {
    /**
     * The namespace for this conductor (e.g., "user", "order").
     * All intents in this conductor will be prefixed with this namespace.
     */
    String namespace() default "";
    
    /**
     * The pattern for this conductor (e.g., "user.*").
     * If specified, this overrides the namespace.
     */
    String pattern() default "";
}
