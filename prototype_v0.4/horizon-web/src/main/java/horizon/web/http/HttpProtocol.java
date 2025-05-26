package horizon.web.http;

import horizon.core.protocol.ProtocolAdapter;
import horizon.core.protocol.ProtocolNames;
import horizon.web.common.AbstractWebProtocol;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * HTTP Protocol implementation for Horizon.
 * This class extends the AbstractWebProtocol to provide HTTP-specific functionality.
 */
public class HttpProtocol extends AbstractWebProtocol<FullHttpRequest, FullHttpResponse> {
    
    private static final int DEFAULT_HTTP_PORT = 8080;
    
    @Override
    public String getName() {
        return ProtocolNames.HTTP;
    }
    
    @Override
    public ProtocolAdapter<FullHttpRequest, FullHttpResponse> createAdapter() {
        return new ConfigurableHttpProtocolAdapter();
    }
    
    @Override
    public int getDefaultPort() {
        return DEFAULT_HTTP_PORT;
    }
    
    @Override
    public String getDescription() {
        return "Hypertext Transfer Protocol - The foundation of data communication for the World Wide Web";
    }
    
    @Override
    public String getVersion() {
        return "HTTP/1.1";
    }
}
