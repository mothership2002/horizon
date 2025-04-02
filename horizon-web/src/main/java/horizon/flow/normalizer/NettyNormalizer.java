package horizon.flow.normalizer;

import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.normalizer.NormalizedInput;
import horizon.protocol.http.HttpRequestFacade;
import horizon.protocol.http.input.netty.NettyHttpRawInput;

public class NettyNormalizer extends AbstractProtocolNormalizer<NettyHttpRawInput> {

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
