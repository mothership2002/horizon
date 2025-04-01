package horizon.parser.normalizer;

import horizon.protocol.http.input.HttpRawInput;
import horizon.protocol.http.HttpRequestFacade;
import horizon.core.flow.parser.normalizer.NormalizedInput;
import horizon.core.flow.parser.normalizer.ProtocolNormalizer;
import horizon.protocol.http.input.netty.NettyHttpRawInput;

public class NettyNormalizer implements ProtocolNormalizer<NettyHttpRawInput> {

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
