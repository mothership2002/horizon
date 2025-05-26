package horizon.core.protocol;

/**
 * Defines a protocol that can be used in the Horizon Framework.
 * Each protocol must provide its adapter for request/response handling.
 * 
 * @param <I> the protocol-specific input type
 * @param <O> the protocol-specific output type
 */
public interface Protocol<I, O> {
    /**
     * Gets the unique name of this protocol.
     */
    String getName();
    
    /**
     * Gets the display name of this protocol.
     */
    default String getDisplayName() {
        return getName();
    }
    
    /**
     * Creates a protocol adapter for this protocol.
     * The adapter handles conversion between protocol-specific and framework formats.
     * 
     * @return a new protocol adapter
     */
    ProtocolAdapter<I, O> createAdapter();
}
