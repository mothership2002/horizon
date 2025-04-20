package horizon.core.rendezvous;

import horizon.core.model.RawInput;

/**
 * Represents a foyer in the Horizon framework.
 * A foyer acts as an entry point for incoming requests and can be used to filter or transform them
 * before they are passed to the rendezvous.
 *
 * @param <I> the type of raw input this foyer can handle
 */
public interface Foyer<I extends RawInput> {

    /**
     * Determines whether the given input should be allowed to proceed to the rendezvous.
     * This method can be used to implement security checks, rate limiting, etc.
     *
     * @param input the raw input to check
     * @return true if the input should be allowed, false otherwise
     */
    boolean allow(I input);

    /**
     * Initializes this foyer.
     * This method should be called before using the foyer.
     */
    default void initialize() {
        // Default implementation does nothing
    }

    /**
     * Shuts down this foyer.
     * This method should be called when the foyer is no longer needed.
     */
    default void shutdown() {
        // Default implementation does nothing
    }
}
