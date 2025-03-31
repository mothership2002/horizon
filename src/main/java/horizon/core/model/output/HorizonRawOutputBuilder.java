package horizon.core.model.output;

import horizon.core.model.output.http.netty.NettyHttpRawOutput;

public class HorizonRawOutputBuilder {

    public static NettyHttpRawOutput build(Object result) {
        return new NettyHttpRawOutput();
    }
}
