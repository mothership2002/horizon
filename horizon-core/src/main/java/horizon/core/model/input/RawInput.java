package horizon.core.model.input;

import horizon.core.constant.Scheme;
import horizon.core.model.Raw;

public interface RawInput extends Raw {

    /**
 * Returns the scheme associated with this raw input.
 *
 * @return a {@link Scheme} instance representing the communication scheme in use
 */
Scheme getScheme();
    /**
 * Retrieves the underlying native request.
 *
 * <p>This method returns an object that encapsulates the raw, platform-specific
 * request data associated with this input.</p>
 *
 * @return the native request object
 */
Object nativeRequest();


}
