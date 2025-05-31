package horizon.core;

/**
 * @deprecated This interface is no longer used. 
 * Use @Conductor annotation on classes and @Intent on methods instead.
 * Will be removed in v0.5.
 * 
 * @see horizon.core.annotation.Conductor
 * @see horizon.core.annotation.Intent
 */
@Deprecated(since = "0.4", forRemoval = true)
public interface Conductor<P, R> {
    R conduct(P payload);
    String getIntentPattern();
}
