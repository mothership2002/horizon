package horizon.web.grpc;

import horizon.core.protocol.ProtocolAdapter;
import horizon.core.protocol.ProtocolNames;
import horizon.web.common.AbstractWebProtocol;

/**
 * Simplified gRPC Protocol implementation for Horizon.
 * 
 * This implementation focuses on:
 * 1. JSON-based communication (no complex protobuf handling)
 * 2. DTO-centric approach
 * 3. Automatic conversion between JSON and Java objects
 * 4. Universal method handling (no pre-defined services)
 */
public class GrpcProtocol extends AbstractWebProtocol<GrpcRequest, GrpcResponse> {
    
    private static final int DEFAULT_GRPC_PORT = 9090;
    
    @Override
    public String getName() {
        return ProtocolNames.GRPC;
    }
    
    @Override
    public String getDisplayName() {
        return "gRPC Protocol";
    }
    
    @Override
    public ProtocolAdapter<GrpcRequest, GrpcResponse> createAdapter() {
        return new GrpcProtocolAdapter();
    }
    
    @Override
    public int getDefaultPort() {
        return DEFAULT_GRPC_PORT;
    }
    
    @Override
    public String getDescription() {
        return "gRPC Protocol - JSON-based, DTO-centric, universal method handling";
    }
    
    @Override
    public String getVersion() {
        return "gRPC/1.0";
    }
    
    @Override
    public boolean supportsSecureConnections() {
        return true;
    }
    
    /**
     * Indicates this is a high-performance protocol.
     */
    public boolean isHighPerformance() {
        return true;
    }
    
    /**
     * Indicates this protocol supports bidirectional streaming.
     */
    public boolean supportsBidirectionalStreaming() {
        return false;  // Not implemented in this simplified version
    }
    
    /**
     * Gets the content type used by this protocol.
     */
    public String getContentType() {
        return "application/grpc";
    }
}
