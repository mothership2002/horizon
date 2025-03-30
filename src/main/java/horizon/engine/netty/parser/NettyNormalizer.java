package horizon.engine.netty.parser;

import horizon.core.input.http.HttpRawInput;
import horizon.core.input.http.HttpRequestFacade;
import horizon.core.parser.NormalizedInput;
import horizon.core.parser.ProtocolNormalizer;

public class NettyNormalizer implements ProtocolNormalizer<HttpRawInput> {

    @Override
    public NormalizedInput normalize(HttpRawInput rawInput) {
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
