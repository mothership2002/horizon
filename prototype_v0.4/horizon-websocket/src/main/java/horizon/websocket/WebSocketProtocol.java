package horizon.websocket;

import horizon.core.protocol.Protocol;
import horizon.core.protocol.ProtocolAdapter;

/**
 * WebSocket Protocol implementation for Horizon.
 */
public class WebSocketProtocol implements Protocol<WebSocketMessage, WebSocketMessage> {
    
    @Override
    public String getName() {
        return "WebSocket";
    }
    
    @Override
    public ProtocolAdapter<WebSocketMessage, WebSocketMessage> createAdapter() {
        return new WebSocketProtocolAdapter();
    }
}
