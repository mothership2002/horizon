package horizon.core.protocol;

/**
 * Resolves intents from protocol-specific requests.
 * 
 * @param <T> the protocol-specific request type
 */
public interface IntentResolver<T> {
    /**
     * Resolves the intent from a protocol-specific request.
     *
     * @param request the protocol-specific request
     * @return the resolved intent string, or null if cannot resolve
     */
    String resolveIntent(T request);
}
