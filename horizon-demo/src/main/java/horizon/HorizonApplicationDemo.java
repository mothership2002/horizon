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
     * Entry point for initializing and running the Horizon framework application.
     * <p>
     * This method sets up the execution and presentation contexts, configures the Netty protocol context (including SentinelScanner-based monitoring),
     * and builds the Netty context using a builder pattern. It then registers the built context with a coordinator and starts all registered contexts.
     *
     * @param args command line arguments (unused)
     * @throws Exception if an error occurs during context initialization or execution
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
