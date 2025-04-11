package horizon.core.flow.rendezvous;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.constant.Scheme;
import horizon.core.exception.InboundSentinelException;
import horizon.core.exception.OutboundSentinelException;
import horizon.core.flow.sentinel.AbstractInboundSentinel;
import horizon.core.flow.sentinel.AbstractOutboundSentinel;
import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.sentinel.FlowSentinel;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.stage.AbstractShadowStage;
import horizon.core.util.SentinelScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@SuppressWarnings("unchecked")
public abstract class AbstractProtocolRendezvous<I extends RawInput, O extends RawOutput> implements ProtocolRendezvous<I, O> {

    private static final Logger log = LoggerFactory.getLogger(AbstractProtocolRendezvous.class);

    protected final ExecutorService rendezvousExecutor;
    protected final List<FlowSentinel.InboundSentinel<I>> inboundSentinels = new LinkedList<>();
    protected final List<FlowSentinel.OutboundSentinel<O>> outboundSentinels = new LinkedList<>();

    protected final AbstractProtocolNormalizer<I> normalizer;
    protected final AbstractProtocolInterpreter interpreter;
    protected final AbstractConductorManager conductorManager;
    protected final RawOutputBuilder<O> rawOutputBuilder;
    protected final AbstractShadowStage shadowStage;

    /**
     * Constructs an AbstractProtocolRendezvous instance with the specified processing components and sentinel configuration.
     *
     * <p>This constructor initializes the protocol processing by setting the provided normalizer, interpreter,
     * conductor manager, output builder, and shadow stage. It also populates the inbound and outbound sentinel lists by
     * retrieving sentinels from the given SentinelScanner based on the scheme's name.</p>
     *
     * @param normalizer       the protocol normalizer for preprocessing input data
     * @param interpreter      the protocol interpreter for interpreting processed requests
     * @param conductorManager the conductor manager coordinating the main processing logic
     * @param rawOutputBuilder the builder used for constructing raw output data
     * @param shadowStage      the manager handling processing stages
     * @param scheme           the scheme whose name is used to fetch the appropriate sentinels
     * @param sentinelScanner  the scanner utility for retrieving inbound and outbound sentinels
     */
    public AbstractProtocolRendezvous(AbstractProtocolNormalizer<I> normalizer, AbstractProtocolInterpreter interpreter,
                                      AbstractConductorManager conductorManager, RawOutputBuilder<O> rawOutputBuilder,
                                      AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner sentinelScanner, ExecutorService rendezvousExecutor) {
        this.normalizer = normalizer;
        this.interpreter = interpreter;
        this.conductorManager = conductorManager;
        this.rawOutputBuilder = rawOutputBuilder;
        this.shadowStage = shadowStage;
        this.rendezvousExecutor = rendezvousExecutor;
        sentinelScanner.getInboundSentinels(scheme.name()).forEach(s -> inboundSentinels.add((FlowSentinel.InboundSentinel<I>) s));
        sentinelScanner.getOutboundSentinels(scheme.name()).forEach(s -> outboundSentinels.add((FlowSentinel.OutboundSentinel<O>) s));
        log.info("Creating protocol rendezvous : {}", getClass().getSimpleName());
    }

    /**
     * Pre-inspects the provided raw input using all registered inbound sentinels.
     *
     * <p>This method iterates over each inbound sentinel and invokes its {@code inspectInbound}
     * method on the raw input. Any {@link InboundSentinelException} thrown during inspection is
     * caught and suppressed.</p>
     *
     * @param rawInput the raw input data to be inspected
     */
    protected void preInspect(I rawInput) {
        try {
            for (FlowSentinel.InboundSentinel<I> s : inboundSentinels) {
                s.inspectInbound(rawInput);
            }
        } catch (InboundSentinelException e) {

        }
    }

    /**
     * Inspects the outbound protocol output using all registered outbound sentinels.
     *
     * <p>This method iterates over each outbound sentinel and invokes its inspection on the provided
     * output. Any {@code OutboundSentinelException} thrown during the inspection is caught and silently ignored,
     * ensuring that the inspection process does not interrupt the normal flow.
     *
     * @param output the protocol output to be inspected
     */
    protected void postInspect(O output) {
        try {
            for (FlowSentinel.OutboundSentinel<O> s : outboundSentinels) {
                s.inspectOutbound(output);
            }
        } catch (OutboundSentinelException e) {

        }
    }
}
