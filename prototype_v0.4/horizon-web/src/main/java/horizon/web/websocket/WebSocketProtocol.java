package horizon.web.websocket;

import horizon.core.protocol.ProtocolAdapter;
import horizon.core.protocol.ProtocolNames;
import horizon.web.common.AbstractWebProtocol;

/**
 * WebSocket Protocol implementation for Horizon.
 * This class extends the AbstractWebProtocol to provide WebSocket-specific functionality.
 */
public class WebSocketProtocol extends AbstractWebProtocol<WebSocketMessage, WebSocketMessage> {
    
    private static final int DEFAULT_WEBSOCKET_PORT = 8081;
    
    @Override
    public String getName() {
        return ProtocolNames.WEBSOCKET;
    }
    
    @Override
    public ProtocolAdapter<WebSocketMessage, WebSocketMessage> createAdapter() {
        return new WebSocketProtocolAdapter();
    }
    
    @Override
    public int getDefaultPort() {
        return DEFAULT_WEBSOCKET_PORT;
    }
    
    @Override
    public String getDescription() {
        return "WebSocket Protocol - Provides full-duplex communication channels over a single TCP connection";
    }
    
    @Override
    public String getVersion() {
        return "RFC 6455";
    }
}
