package horizon.core.model;

/**
 * A marker interface that represents raw data in the Horizon framework.
 * This interface serves as a common type for both input and output data,
 * allowing for type-safe handling of data throughout the framework.
 * 
 * Implementations of this interface should provide specific methods
 * for accessing and manipulating the raw data they represent.
 */
public interface Raw {
    /**
     * Returns metadata associated with this raw data.
     * Implementations can define what constitutes metadata for their specific context.
     * 
     * @return an Object containing metadata, or null if no metadata is available
     */
    default Object getMetadata() {
        return null;
    }
}
