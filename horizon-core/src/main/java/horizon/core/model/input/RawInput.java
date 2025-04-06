package horizon.core.model.input;

import horizon.core.constant.Scheme;
import horizon.core.model.Raw;

public interface RawInput extends Raw {

    Scheme getScheme();
    Object nativeRequest();


}
