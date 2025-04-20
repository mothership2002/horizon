package horizon.demo.http;

import horizon.http.netty.NettyInputConverter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Objects;

/**
 * A simple implementation of NettyInputConverter for HTTP requests.
 */
public class SimpleHttpInputConverter implements NettyInputConverter<SimpleHttpInput> {

    /**
     * Converts a Netty HTTP request to a SimpleHttpInput.
     *
     * @param request the Netty HTTP request to convert
     * @param remoteAddress the remote address of the client
     * @return the converted SimpleHttpInput
     * @throws IllegalArgumentException if the request cannot be converted
     * @throws NullPointerException if request is null
     */
    @Override
    public SimpleHttpInput convert(FullHttpRequest request, String remoteAddress) 
            throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(remoteAddress, "remoteAddress must not be null");

        // Get the path from the request
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        String path = decoder.path();

        // Get the HTTP method
        String method = request.method().name();

        // Get the body
        byte[] body = new byte[request.content().readableBytes()];
        request.content().getBytes(request.content().readerIndex(), body);

        // Create and return the SimpleHttpInput
        return new SimpleHttpInput(remoteAddress, "http", body, path, method);
    }
}