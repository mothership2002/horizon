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

@SuppressWarnings("unchecked")
public abstract class AbstractProtocolRendezvous<I extends RawInput, O extends RawOutput> implements ProtocolRendezvous<I, O> {

    private static final Logger log = LoggerFactory.getLogger(AbstractProtocolRendezvous.class);

    protected final List<FlowSentinel.InboundSentinel<I>> inboundSentinels = new LinkedList<>();
    protected final List<FlowSentinel.OutboundSentinel<O>> outboundSentinels = new LinkedList<>();

    protected final AbstractProtocolNormalizer<I> normalizer;
    protected final AbstractProtocolInterpreter interpreter;
    protected final AbstractConductorManager conductorManager;
    protected final RawOutputBuilder<O> rawOutputBuilder;
    protected final AbstractShadowStage shadowStage;

    /**
     * Initializes a new protocol rendezvous instance.
     *
     * <p>
     * This constructor configures the protocol processing pipeline by setting up the provided
     * normalizer, interpreter, conductor manager, raw output builder, and shadow stage. It also
     * retrieves inbound and outbound sentinels associated with the specified scheme via the given
     * sentinel scanner, and logs the creation of the protocol rendezvous instance.
     * </p>
     *
     * @param normalizer       the protocol normalizer used to process raw input data
     * @param interpreter      the protocol interpreter used for parsing requests
     * @param conductorManager the manager responsible for orchestrating the conduction of parsed requests
     * @param rawOutputBuilder the builder used to create raw output responses
     * @param shadowStage      the stage used for additional processing steps
     * @param scheme           the protocol scheme identifier used to locate relevant sentinels
     * @param sentinelScanner  the scanner that retrieves inbound and outbound sentinels for the scheme
     */
    public AbstractProtocolRendezvous(AbstractProtocolNormalizer<I> normalizer, AbstractProtocolInterpreter interpreter,
                                      AbstractConductorManager conductorManager, RawOutputBuilder<O> rawOutputBuilder,
                                      AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner sentinelScanner) {
        this.normalizer = normalizer;
        this.interpreter = interpreter;
        this.conductorManager = conductorManager;
        this.rawOutputBuilder = rawOutputBuilder;
        this.shadowStage = shadowStage;
        sentinelScanner.getInboundSentinels(scheme.name()).forEach(s -> inboundSentinels.add((FlowSentinel.InboundSentinel<I>) s));
        sentinelScanner.getOutboundSentinels(scheme.name()).forEach(s -> outboundSentinels.add((FlowSentinel.OutboundSentinel<O>) s));
        log.info("Creating protocol rendezvous : {}", getClass().getSimpleName());
    }

    /**
     * Processes the parsed request and returns the generated output.
     *
     * <p>This method directs the parsed request through the conductor manager, constructs the output using the raw output builder, 
     * and applies outbound post-inspection on the created output before returning it.</p>
     *
     * @param parsed the parsed request to be processed
     * @return the generated output after post-inspection
     */
    protected O afterConduct(ParsedRequest parsed) {
        Object result = conductorManager.conduct(parsed);
        O output = rawOutputBuilder.build(result);
        postInspect(output);
        return output;
    }

    /**
     * Inspects the provided raw input using all registered inbound sentinels.
     *
     * <p>This method iterates over each inbound sentinel, invoking their inspection logic on the raw input.
     * Any InboundSentinelException encountered during inspection is silently caught.</p>
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
     * Applies outbound inspections on the provided output using all registered outbound sentinels.
     * <p>
     * Iterates over each outbound sentinel and invokes its {@code inspectOutbound} method. Any thrown
     * {@code OutboundSentinelException} is caught and suppressed.
     *
     * @param output the output to be inspected
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
