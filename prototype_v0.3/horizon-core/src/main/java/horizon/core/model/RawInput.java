package horizon.core.model;

/**
 * Represents raw input data in the Horizon framework.
 * This interface extends the Raw marker interface and provides methods
 * for accessing common properties of input data.
 */
public interface RawInput extends Raw {

    /**
     * Returns the source of this input.
     * The source typically represents the origin or sender of the input.
     *
     * @return a non-null String representing the source
     */
    String getSource();

    /**
     * Returns the scheme of this input.
     * The scheme represents the protocol or format of the input (e.g., http, cli, ws).
     *
     * @return a non-null String representing the scheme
     */
    String getScheme();

    /**
     * Returns the body of this input as a byte array.
     * The body contains the actual payload or content of the input.
     *
     * @return a byte array containing the body, or null if no body is available
     */
    byte[] getBody();

    /**
     * Validates this input.
     * Implementations should check if the input is valid according to their specific rules.
     *
     * @return true if the input is valid, false otherwise
     */
    default boolean isValid() {
        return getSource() != null && getScheme() != null;
    }
}
