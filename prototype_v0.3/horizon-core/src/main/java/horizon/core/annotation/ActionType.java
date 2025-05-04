package horizon.core.annotation;

/**
 * Enumeration of common action types.
 * This enum provides a protocol-agnostic way to specify the type of action
 * that an intent represents.
 */
public enum ActionType {
    /**
     * Read or retrieve data.
     */
    READ,
    
    /**
     * Create new data.
     */
    CREATE,
    
    /**
     * Update existing data.
     */
    UPDATE,
    
    /**
     * Delete data.
     */
    DELETE,
    
    /**
     * Query or search for data.
     */
    QUERY,
    
    /**
     * Execute a command or action.
     */
    EXECUTE,
    
    /**
     * Subscribe to events or notifications.
     */
    SUBSCRIBE,
    
    /**
     * Unsubscribe from events or notifications.
     */
    UNSUBSCRIBE
}