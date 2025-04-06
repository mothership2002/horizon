package horizon.core.model.input;

import horizon.core.constant.Scheme;
import horizon.core.model.Raw;

public interface RawInput extends Raw {

    /**
 * Retrieves the communication scheme associated with the raw input.
 *
 * <p>This method returns the scheme defined in {@code horizon.core.constant.Scheme}, which indicates
 * the protocol or configuration used for the input data (e.g., HTTP, HTTPS). It allows consumers of the
 * input to adapt processing based on the underlying scheme.</p>
 *
 * @return the scheme associated with the raw input
 */
Scheme getScheme();
    /**
 * Retrieves the raw request data in its native form.
 *
 * <p>This method provides direct access to the underlying request object as it was originally received.
 *
 * @return the native request object.
 */
Object nativeRequest();


}
