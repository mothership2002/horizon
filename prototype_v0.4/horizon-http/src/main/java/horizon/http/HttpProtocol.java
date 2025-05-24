package horizon.http;

import horizon.core.protocol.Protocol;
import horizon.core.protocol.ProtocolAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * HTTP Protocol implementation for Horizon.
 */
public class HttpProtocol implements Protocol<FullHttpRequest, FullHttpResponse> {
    
    @Override
    public String getName() {
        return "HTTP";
    }
    
    @Override
    public ProtocolAdapter<FullHttpRequest, FullHttpResponse> createAdapter() {
        return new HttpProtocolAdapter();
    }
}
