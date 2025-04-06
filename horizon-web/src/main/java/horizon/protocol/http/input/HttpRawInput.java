package horizon.protocol.http.input;

import horizon.core.constant.Scheme;
import horizon.core.model.input.RawInput;
import horizon.protocol.http.HttpRequestFacade;

public abstract class HttpRawInput implements RawInput {

    @Override
    public Scheme getScheme() {
        return Scheme.http;
    }

    public abstract Object nativeRequest();

    public abstract HttpRequestFacade getRequestFacade();
}
