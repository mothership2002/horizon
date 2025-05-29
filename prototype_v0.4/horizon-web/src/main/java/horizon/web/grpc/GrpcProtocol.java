package horizon.web.grpc;

import horizon.core.protocol.ProtocolAdapter;
import horizon.core.protocol.ProtocolNames;
import horizon.web.common.AbstractWebProtocol;

/**
 * gRPC Protocol implementation for Horizon.
 * Enables gRPC services to be exposed through the Horizon framework.
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
        return "gRPC - A high-performance, open source universal RPC framework";
    }
    
    @Override
    public String getVersion() {
        return "gRPC/1.0";
    }
}
