package horizon.core.protocol;

/**
 * Defines a protocol that can be used in the Horizon Framework.
 * This is a marker interface that all protocol definitions must implement.
 */
public interface Protocol {
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
}
