package horizon.core.protocol;

/**
 * Adapts protocol-specific requests and responses to the Horizon format.
 * This is the bridge between diverse protocols and the unified Horizon processing.
 *
 * @param <I> the protocol-specific input type
 * @param <O> the protocol-specific output type
 */
public interface ProtocolAdapter<I, O> {
    
    /**
     * Extracts the intent from a protocol-specific request.
     *
     * @param request the protocol-specific request
     * @return the intent string (e.g., "user.create")
     */
    String extractIntent(I request);
    
    /**
     * Extracts the payload from a protocol-specific request.
     *
     * @param request the protocol-specific request
     * @return the payload object
     */
    Object extractPayload(I request);
    
    /**
     * Builds a protocol-specific response from the result.
     *
     * @param result the processing result
     * @param request the original request (for context)
     * @return the protocol-specific response
     */
    O buildResponse(Object result, I request);
    
    /**
     * Builds a protocol-specific error response.
     *
     * @param error the error that occurred
     * @param request the original request (for context)
     * @return the protocol-specific error response
     */
    O buildErrorResponse(Throwable error, I request);
}
