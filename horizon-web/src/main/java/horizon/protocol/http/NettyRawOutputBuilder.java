package horizon.protocol.http;


import horizon.core.model.RawOutputBuilder;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;

public class NettyRawOutputBuilder implements RawOutputBuilder<NettyHttpRawOutput> {

    @Override
    public NettyHttpRawOutput build(Object result) {
        return new NettyHttpRawOutput(result);
    }
}
