package horizon.flow.normalizer;

import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.normalizer.NormalizedInput;
import horizon.protocol.http.HttpRequestFacade;
import horizon.protocol.http.input.netty.NettyHttpRawInput;

public class NettyNormalizer extends AbstractProtocolNormalizer<NettyHttpRawInput> {

    /**
     * Constructs a new NettyNormalizer instance.
     *
     * <p>This no-argument constructor initializes the instance by invoking the superclass constructor.
     */
    public NettyNormalizer() {
        super();
    }

    /**
     * Converts a Netty HTTP raw input into a normalized HTTP request representation.
     *
     * <p>This method extracts key elements from the provided {@code NettyHttpRawInput}, including the
     * scheme, HTTP method, path, headers, query parameters, body, and native request, and consolidates
     * them into a {@code NormalizedInput} instance.</p>
     *
     * @param rawInput the raw HTTP input data from Netty
     * @return a normalized representation of the HTTP request
     */
    @Override
    public NormalizedInput normalize(NettyHttpRawInput rawInput) {
        HttpRequestFacade request = rawInput.getRequestFacade();
        return new NormalizedInput(
                rawInput.getScheme(),
                request.getMethod(),
                request.getPath(),
                request.getHeaders(),
                request.getQueryParams(),
                request.getBody(),
                rawInput.nativeRequest()
        );
    }

}
