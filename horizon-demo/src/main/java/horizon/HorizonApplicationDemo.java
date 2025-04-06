package horizon;

import horizon.app.context.DefaultPresentationContext;
import horizon.context.NettyContext;
import horizon.context.NettyProperties;
import horizon.context.NettyProtocolContext;
import horizon.core.annotation.HorizonApplication;
import horizon.core.constant.Scheme;
import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.HorizonContextCoordinator;
import horizon.core.util.HorizonContextBuilder;
import horizon.core.util.SentinelScanner;
import horizon.data.context.DefaultExecutionContext;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;

@HorizonApplication
public class HorizonApplicationDemo {

    /**
     * Main entry point for launching the Horizon application.
     *
     * <p>This method initializes the core contexts required for the application, including the execution,
     * presentation, and protocol contexts configured for HTTP communication via Netty. It also builds the
     * Netty properties and registers the constructed context with the HorizonContextCoordinator to start
     * the application. Future enhancements may include support for a multi-protocol server.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during initialization
     */
    public static void main(String[] args) throws Exception {
        HorizonContextCoordinator coordinator = new HorizonContextCoordinator();
        DefaultExecutionContext execution = new DefaultExecutionContext();
        DefaultPresentationContext presentation = new DefaultPresentationContext();
        SentinelScanner sentinelScanner = SentinelScanner.auto();
        AbstractHorizonContext.AbstractProtocolContext<NettyHttpRawInput, NettyHttpRawOutput> protocol
                = new NettyProtocolContext(presentation.provideConductorManager(), execution.provideShadowStage(), Scheme.http, sentinelScanner);

        NettyProperties properties = NettyProperties.builder().build();

        AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> netty = HorizonContextBuilder.<NettyHttpRawInput, NettyHttpRawOutput>builder()
                .withExecutionContext(execution)
                .withPresentationContext(presentation)
                .withProtocolContext(protocol)
                .withProperties(properties)
                .build(NettyContext.class);

        coordinator.register(netty);
        // TODO multi protocol server
        coordinator.runAll();
    }
}
