package horizon.core.model.input;

import horizon.core.model.Raw;

public interface RawInput extends Raw {

    Scheme getScheme();
    Object nativeRequest();

    enum Scheme {
        http,
        https,
        websocket,
        websocketSsl,
        gRpc,
        cli,
        unknown;
    }
}
