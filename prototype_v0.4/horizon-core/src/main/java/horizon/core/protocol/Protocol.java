package horizon.core.protocol;

/**
 * Represents a communication protocol that can be aggregated by Horizon.
 * Each protocol knows how to adapt its specific format to the common Horizon format.
 *
 * @param <I> the protocol-specific input type
 * @param <O> the protocol-specific output type
 */
public interface Protocol<I, O> {
    
    /**
     * Returns the name of this protocol (e.g., "HTTP", "WebSocket", "gRPC").
     *
     * @return the protocol name
     */
    String getName();
    
    /**
     * Creates an adapter for this protocol.
     * The adapter handles the conversion between protocol-specific and Horizon formats.
     *
     * @return a protocol adapter
     */
    ProtocolAdapter<I, O> createAdapter();
}
