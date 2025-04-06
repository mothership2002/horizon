package horizon.core.model.input;

import horizon.core.constant.Scheme;
import horizon.core.model.Raw;

public interface RawInput extends Raw {

    /**
 * Retrieves the scheme associated with this raw input.
 *
 * This method returns the communication protocol (e.g., HTTP, HTTPS, WebSocket) as defined
 * in the external {@code Scheme} enum from the {@code horizon.core.constant} package.
 *
 * @return the communication scheme for this input.
 */
Scheme getScheme();
    /**
 * Returns the native request object associated with this input.
 *
 * <p>This method provides access to the underlying native request, which represents the raw data
 * of the request. The returned object can be used for operations requiring low-level access.
 *
 * @return the native request object
 */
Object nativeRequest();


}
