package horizon.flow.normalizer;

import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.normalizer.NormalizedInput;
import horizon.protocol.http.HttpRequestFacade;
import horizon.protocol.http.input.netty.NettyHttpRawInput;

public class NettyNormalizer extends AbstractProtocolNormalizer<NettyHttpRawInput> {

    /**
     * Constructs a new NettyNormalizer instance.
     *
     * <p>This explicit constructor calls the superclass constructor,
     * setting up the normalizer for future extensions.
     */
    public NettyNormalizer() {
        super();
    }

    /**
     * Normalizes the provided Netty HTTP raw input into a common format.
     *
     * <p>This method extracts the HTTP request details from the given raw input,
     * including the scheme, HTTP method, URI path, headers, query parameters, body,
     * and underlying native request. These details are used to construct a 
     * NormalizedInput object for further processing.
     *
     * @param rawInput the Netty HTTP raw input containing the original request data
     * @return a NormalizedInput instance with the normalized request details
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
