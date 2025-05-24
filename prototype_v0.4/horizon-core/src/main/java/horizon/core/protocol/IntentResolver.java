package horizon.core.protocol;

/**
 * Strategy interface for resolving intents from protocol-specific requests.
 * Allows customization of how intents are extracted from different protocols.
 */
public interface IntentResolver<T> {
    /**
     * Resolves the intent from a protocol-specific request.
     *
     * @param request the protocol-specific request
     * @return the resolved intent string
     */
    String resolveIntent(T request);
    
    /**
     * Checks if this resolver can handle the given request.
     *
     * @param request the protocol-specific request
     * @return true if this resolver can handle the request
     */
    default boolean canResolve(T request) {
        return true;
    }
}
