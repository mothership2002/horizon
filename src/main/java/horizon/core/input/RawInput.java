package horizon.core.input;

public interface RawInput {

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
