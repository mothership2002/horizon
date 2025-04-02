package horizon.context;

import horizon.core.conductor.ConductorManager;
import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.ServerEngine;
import horizon.core.flow.centinel.FlowSentinelInterface;
import horizon.core.flow.foyer.AbstractProtocolFoyer;
import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.rendezvous.AbstractProtocolRendezvous;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.engine.netty.HorizonNettyBootstrap;
import horizon.flow.foyer.NettyFoyer;
import horizon.flow.interpreter.NettyInterpreter;
import horizon.flow.normalizer.NettyNormalizer;
import horizon.flow.rendezvous.DefaultRendezvous;
import horizon.protocol.http.NettyRawOutputBuilder;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;

import java.util.List;
import java.util.Set;

import static horizon.core.util.SentinelScanner.scanInbound;
import static horizon.core.util.SentinelScanner.scanOutbound;

public class NettyEngineContext extends AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> {

    //...
    private final int eventLoopGroupSize;
    private final int workerThreadSize;
    private final int port;
    private final int maxContentLength;//
    // time out
    private final int readTimeoutMillis;
    private final int allIdleTimeMillis;
    private final int writeTimeoutMillis;
    // http codec
    private final int maxInitialLineLength;
    private final int maxHeaderSize;
    private final int maxChunkSize;


    public NettyEngineContext(Integer eventLoopGroupSize, Integer workerThreadSize, Integer port, Integer maxContentLength,
                              Integer readTimeoutMillis, Integer allIdleTimeMillis, Integer writeTimeoutMillis,
                              Integer maxInitialLineLength, Integer maxHeaderSize, Integer maxChunkSize) {
        this.eventLoopGroupSize = eventLoopGroupSize;
        this.workerThreadSize = workerThreadSize;
        this.port = port;
        this.maxContentLength = maxContentLength;
        this.readTimeoutMillis = readTimeoutMillis;
        this.allIdleTimeMillis = allIdleTimeMillis;
        this.writeTimeoutMillis = writeTimeoutMillis;
        this.maxInitialLineLength = maxInitialLineLength;
        this.maxHeaderSize = maxHeaderSize;
        this.maxChunkSize = maxChunkSize;
    }

    public NettyEngineContext(Integer eventLoopGroupSize, Integer workerThreadSize, Integer port, Integer maxContentLength,
                              Integer readTimeoutMillis, Integer allIdleTimeMillis, Integer writeTimeoutMillis,
                              Integer maxInitialLineLength, Integer maxHeaderSize) {
        this(eventLoopGroupSize, workerThreadSize, port, maxContentLength, readTimeoutMillis, allIdleTimeMillis, writeTimeoutMillis,
                maxInitialLineLength, maxHeaderSize, 4096);
    }

    public NettyEngineContext(Integer eventLoopGroupSize, Integer workerThreadSize, Integer port, Integer maxContentLength,
                              Integer readTimeoutMillis, Integer allIdleTimeMillis, Integer writeTimeoutMillis,
                              Integer maxInitialLineLength) {
        this(eventLoopGroupSize, workerThreadSize, port, maxContentLength, readTimeoutMillis, allIdleTimeMillis, writeTimeoutMillis,
                maxInitialLineLength, 4096);
    }

    public NettyEngineContext(Integer eventLoopGroupSize, Integer workerThreadSize, Integer port, Integer maxContentLength,
                              Integer readTimeoutMillis, Integer allIdleTimeMillis, Integer writeTimeoutMillis) {
        this(eventLoopGroupSize, workerThreadSize, port, maxContentLength, readTimeoutMillis, allIdleTimeMillis, writeTimeoutMillis, 8192);
    }

    public NettyEngineContext(Integer eventLoopGroupSize, Integer workerThreadSize, Integer port, Integer maxContentLength,
                              Integer readTimeoutMillis, Integer allIdleTimeMillis) {
        this(eventLoopGroupSize, workerThreadSize, port, maxContentLength, readTimeoutMillis, allIdleTimeMillis, 10000);
    }

    public NettyEngineContext(Integer eventLoopGroupSize, Integer workerThreadSize, Integer port, Integer maxContentLength,
                              Integer readTimeoutMillis) {
        this(eventLoopGroupSize, workerThreadSize, port, maxContentLength, readTimeoutMillis, 10000);
    }

    public NettyEngineContext(Integer eventLoopGroupSize, Integer workerThreadSize, Integer port, Integer maxContentLength) {
        this(eventLoopGroupSize, workerThreadSize, port, maxContentLength, 10000);
    }

    public NettyEngineContext(Integer eventLoopGroupSize, Integer workerThreadSize, Integer port) {
        this(eventLoopGroupSize, workerThreadSize, port, 1024 * 1024 * 4);
    }

    public NettyEngineContext(Integer workerThreadSize, Integer port) {
        this(1, workerThreadSize, port);
    }

    public NettyEngineContext(Integer port) {
        this(1, 0, port);
    }

    @Override
    public AbstractProtocolNormalizer<NettyHttpRawInput> provideNormalizer() {
        return new NettyNormalizer();
    }

    @Override
    public AbstractProtocolInterpreter provideInterpreter() {
        return new NettyInterpreter();
    }

    @Override
    public ConductorManager provideConductorManager() {
        return new ConductorManager();
    }

    @Override
    public ServerEngine.ServerEngineTemplate<NettyHttpRawInput, NettyHttpRawOutput> provideEngine() {
        return new HorizonNettyBootstrap();
    }

    @Override
    public AbstractProtocolFoyer<NettyHttpRawInput> provideFoyer() {
        return new NettyFoyer<>(initializePipeline());
    }

    @Override
    public RawOutputBuilder<NettyHttpRawOutput> provideOutputBuilder() {
        return new NettyRawOutputBuilder();
    }

    @Override
    protected AbstractProtocolRendezvous<NettyHttpRawInput, NettyHttpRawOutput> initializePipeline() {
        AbstractProtocolRendezvous<NettyHttpRawInput, NettyHttpRawOutput> rendezvous
                = new DefaultRendezvous<>(provideNormalizer(), provideInterpreter(), provideConductorManager(), provideOutputBuilder());
        scanInboundSentinels().forEach(rendezvous::addInboundSentinel);
        scanOutboundSentinels().forEach(rendezvous::addOutboundSentinel);
        return rendezvous;
    }

    @Override
    protected List<FlowSentinelInterface.InboundSentinel<NettyHttpRawInput>> scanInboundSentinels() {
        return scanInbound(Set.of(RawInput.Scheme.http, RawInput.Scheme.https));
    }

    @Override
    protected List<FlowSentinelInterface.OutboundSentinel<NettyHttpRawOutput>> scanOutboundSentinels() {
        return scanOutbound(Set.of(RawInput.Scheme.http, RawInput.Scheme.https));
    }

    public int getEventLoopGroupSize() {
        return eventLoopGroupSize;
    }

    public int getWorkerThreadSize() {
        return workerThreadSize;
    }

    public Integer getPort() {
        return port;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public int getAllIdleTimeMillis() {
        return allIdleTimeMillis;
    }

    public int getWriteTimeoutMillis() {
        return writeTimeoutMillis;
    }

    public int getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    public int getMaxHeaderSize() {
        return maxHeaderSize;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }


}