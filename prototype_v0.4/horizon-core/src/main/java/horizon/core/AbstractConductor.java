package horizon.core;

/**
 * Abstract base class for Conductors that provides common functionality.
 * Simplifies conductor implementation by requiring only the business logic.
 */
public abstract class AbstractConductor<P, R> implements Conductor<P, R> {
    private final String intentPattern;
    
    protected AbstractConductor(String intentPattern) {
        this.intentPattern = intentPattern;
    }
    
    @Override
    public String getIntentPattern() {
        return intentPattern;
    }
}
