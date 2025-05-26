package horizon.web.common;

import horizon.core.protocol.ProtocolAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for web protocol adapters (HTTP and WebSocket).
 * This class provides common functionality for web protocol adapters.
 *
 * @param <I> the protocol-specific input type
 * @param <O> the protocol-specific output type
 */
public abstract class AbstractWebProtocolAdapter<I, O> implements ProtocolAdapter<I, O> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractWebProtocolAdapter.class);
    
    @Override
    public String extractIntent(I request) {
        try {
            return doExtractIntent(request);
        } catch (Exception e) {
            logger.error("Failed to extract intent", e);
            throw new RuntimeException("Failed to extract intent", e);
        }
    }
    
    @Override
    public Object extractPayload(I request) {
        try {
            return doExtractPayload(request);
        } catch (Exception e) {
            logger.error("Failed to extract payload", e);
            throw new RuntimeException("Failed to extract payload", e);
        }
    }
    
    @Override
    public O buildResponse(Object result, I request) {
        try {
            return doBuildResponse(result, request);
        } catch (Exception e) {
            logger.error("Failed to build response", e);
            return buildErrorResponse(e, request);
        }
    }
    
    @Override
    public O buildErrorResponse(Throwable error, I request) {
        try {
            return doBuildErrorResponse(error, request);
        } catch (Exception e) {
            logger.error("Failed to build error response", e);
            // This is a last resort fallback
            return createFallbackErrorResponse(e, request);
        }
    }
    
    /**
     * Extracts the intent from a protocol-specific request.
     * This method is called by {@link #extractIntent(Object)}.
     *
     * @param request the protocol-specific request
     * @return the intent string
     */
    protected abstract String doExtractIntent(I request);
    
    /**
     * Extracts the payload from a protocol-specific request.
     * This method is called by {@link #extractPayload(Object)}.
     *
     * @param request the protocol-specific request
     * @return the payload object
     */
    protected abstract Object doExtractPayload(I request);
    
    /**
     * Builds a protocol-specific response from the result.
     * This method is called by {@link #buildResponse(Object, Object)}.
     *
     * @param result the processing result
     * @param request the original request (for context)
     * @return the protocol-specific response
     */
    protected abstract O doBuildResponse(Object result, I request);
    
    /**
     * Builds a protocol-specific error response.
     * This method is called by {@link #buildErrorResponse(Throwable, Object)}.
     *
     * @param error the error that occurred
     * @param request the original request (for context)
     * @return the protocol-specific error response
     */
    protected abstract O doBuildErrorResponse(Throwable error, I request);
    
    /**
     * Creates a fallback error response when even the error response builder fails.
     * This is a last resort to avoid cascading failures.
     *
     * @param error the error that occurred
     * @param request the original request (for context)
     * @return a minimal error response
     */
    protected abstract O createFallbackErrorResponse(Throwable error, I request);
}