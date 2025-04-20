package horizon.core.model;

/**
 * Represents raw output data in the Horizon framework.
 * This interface extends the Raw marker interface and provides methods
 * for accessing and manipulating output data.
 * 
 * Implementations should provide specific methods for handling different types
 * of output data based on their context and requirements.
 */
public interface RawOutput extends Raw {

    /**
     * Returns the content of this output.
     * The content represents the actual data to be sent back to the client.
     *
     * @return an Object representing the content, or null if no content is available
     */
    default Object getContent() {
        return null;
    }

    /**
     * Returns the status code of this output.
     * The status code typically represents the result of the operation
     * (e.g., success, failure, error).
     *
     * @return an integer representing the status code, or 0 if no status code is available
     */
    default int getStatusCode() {
        return 0;
    }

    /**
     * Returns whether this output represents a successful operation.
     *
     * @return true if the operation was successful, false otherwise
     */
    default boolean isSuccess() {
        return getStatusCode() >= 200 && getStatusCode() < 300;
    }
}
