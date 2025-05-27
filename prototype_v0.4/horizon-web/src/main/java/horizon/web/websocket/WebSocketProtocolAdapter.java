package horizon.web.websocket;

import horizon.core.ProtocolAggregator;
import horizon.core.protocol.AggregatorAware;
import horizon.web.common.AbstractWebProtocolAdapter;
import horizon.web.common.PayloadExtractor;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts WebSocket messages to Horizon format.
 * This class extends AbstractWebProtocolAdapter to provide WebSocket-specific functionality.
 */
public class WebSocketProtocolAdapter extends AbstractWebProtocolAdapter<WebSocketMessage, WebSocketMessage> 
        implements AggregatorAware {
    
    private PayloadExtractor payloadExtractor;
    private ProtocolAggregator aggregator;
    
    /**
     * Sets the protocol aggregator for accessing conductor metadata.
     * This enables automatic DTO conversion based on conductor method parameters.
     */
    @Override
    public void setProtocolAggregator(ProtocolAggregator aggregator) {
        this.aggregator = aggregator;
        this.payloadExtractor = new PayloadExtractor(aggregator);
    }
    
    @Override
    protected String doExtractIntent(WebSocketMessage message) {
        return message.getIntent();
    }
    
    @Override
    protected Object doExtractPayload(WebSocketMessage message) {
        String intent = message.getIntent();
        
        if (payloadExtractor != null) {
            // Use the unified payload extractor
            return payloadExtractor.extractWebSocketPayload(message.getData(), message.getSessionId(), intent);
        } else {
            // Fallback to simple Map extraction if aggregator not set
            return extractPayloadAsMap(message);
        }
    }
    
    /**
     * Simple payload extraction as Map (fallback method).
     */
    private Map<String, Object> extractPayloadAsMap(WebSocketMessage message) {
        Map<String, Object> payload = new HashMap<>();
        if (message.getData() != null) {
            payload.putAll(message.getData());
        }
        payload.put("_sessionId", message.getSessionId());
        return payload;
    }
    
    @Override
    protected WebSocketMessage doBuildResponse(Object result, WebSocketMessage request) {
        WebSocketMessage response = new WebSocketMessage();
        response.setIntent(request.getIntent() + ".response");
        
        Map<String, Object> data = new HashMap<>();
        data.put("result", result);
        data.put("success", true);
        response.setData(data);
        
        response.setSessionId(request.getSessionId());
        return response;
    }
    
    @Override
    protected WebSocketMessage doBuildErrorResponse(Throwable error, WebSocketMessage request) {
        WebSocketMessage response = new WebSocketMessage();
        response.setIntent(request.getIntent() + ".error");
        
        Map<String, Object> data = new HashMap<>();
        data.put("error", error.getMessage());
        data.put("type", error.getClass().getSimpleName());
        data.put("success", false);
        response.setData(data);
        
        response.setSessionId(request.getSessionId());
        return response;
    }
    
    @Override
    protected WebSocketMessage createFallbackErrorResponse(Throwable error, WebSocketMessage request) {
        WebSocketMessage response = new WebSocketMessage();
        response.setIntent("error");
        
        Map<String, Object> data = new HashMap<>();
        data.put("error", "Internal server error");
        data.put("success", false);
        response.setData(data);
        
        response.setSessionId(request.getSessionId());
        return response;
    }
}
