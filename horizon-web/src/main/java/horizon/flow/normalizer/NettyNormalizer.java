package horizon.flow.normalizer;

import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.normalizer.NormalizedInput;
import horizon.protocol.http.HttpRequestFacade;
import horizon.protocol.http.input.netty.NettyHttpRawInput;

public class NettyNormalizer extends AbstractProtocolNormalizer<NettyHttpRawInput> {

    /**
     * Constructs a new NettyNormalizer.
     *
     * <p>This constructor initializes a new instance by invoking the superclass constructor,
     * setting up the normalizer to handle NettyHttpRawInput objects.</p>
     */
    public NettyNormalizer() {
        super();
    }

    /**
     * Normalizes the provided Netty HTTP raw input into a standardized request representation.
     *
     * <p>This method obtains the HTTP request facade from the raw input and constructs a
     * {@code NormalizedInput} with the input's scheme, HTTP method, path, headers, query parameters,
     * body, and native request.</p>
     *
     * @param rawInput the Netty HTTP raw input to be normalized
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
