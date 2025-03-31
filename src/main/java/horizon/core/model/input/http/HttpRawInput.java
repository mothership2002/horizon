package horizon.core.model.input.http;

import horizon.core.model.input.RawInput;

public abstract class HttpRawInput implements RawInput {

    @Override
    public Scheme getScheme() {
        return Scheme.http;
    }

    public abstract Object nativeRequest();

    public abstract HttpRequestFacade getRequestFacade();
}
