package horizon.engine.netty;

import horizon.core.context.HorizonContext;
import horizon.core.model.input.http.HttpRawInput;
import horizon.core.model.output.http.HttpRawOutput;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class HorizonChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final HorizonContext<HttpRawInput, HttpRawOutput> context;

    public HorizonChannelInitializer(HorizonContext<HttpRawInput, HttpRawOutput> context) {
        this.context = context;
    }

    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new HorizonNettyAdapter(context.provideProcessor()));
    }
}
