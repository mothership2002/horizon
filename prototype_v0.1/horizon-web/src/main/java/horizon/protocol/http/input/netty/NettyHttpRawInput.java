package horizon.protocol.http.input.netty;

import horizon.protocol.http.input.HttpRawInput;
import horizon.protocol.http.HttpRequestFacade;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class NettyHttpRawInput extends HttpRawInput {

    private final FullHttpRequest request;
    private final ChannelHandlerContext context;

    public NettyHttpRawInput(FullHttpRequest request, ChannelHandlerContext context) {
        this.request = request;
        this.context = context;
    }

    @Override
    public Object nativeRequest() {
        return request;
    }

    @Override
    public HttpRequestFacade getRequestFacade() {
        return new HttpRequestFacade.NettyHttpRequestFacade(request);
    }

}
