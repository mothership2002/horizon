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
     * Constructs a new protocol rendezvous instance for managing raw protocol input and output.
     *
     * <p>This constructor sets up the protocol components for input normalization, request
     * interpretation, request conduction, output building, and shadow stage management. It also
     * retrieves and registers inbound and outbound sentinels based on the provided scheme using the
     * supplied sentinel scanner, and logs the creation of the rendezvous.
     *
     * @param normalizer      the component that normalizes raw input data
     * @param interpreter     the component responsible for interpreting protocol requests
     * @param conductorManager the manager that executes parsed requests
     * @param rawOutputBuilder the builder used to construct raw output from processed requests
     * @param shadowStage     the stage that manages shadow operations within the protocol flow
     * @param scheme          the protocol scheme used to identify and retrieve appropriate sentinels
     * @param sentinelScanner the scanner that provides inbound and outbound sentinels for the given scheme
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
     * Processes a parsed request to generate the corresponding protocol output.
     *
     * <p>This method conducts the parsed request via the conductor manager, builds the raw output using the output builder,
     * and then performs a post-inspection on the resulting output.</p>
     *
     * @param parsed the parsed request to process
     * @return the output generated from the conduction result after post-inspection
     */
    protected O afterConduct(ParsedRequest parsed) {
        Object result = conductorManager.conduct(parsed);
        O output = rawOutputBuilder.build(result);
        postInspect(output);
        return output;
    }

    /**
     * Inspects the inbound raw input by invoking the inspection logic of each inbound sentinel.
     * <p>
     * Iterates through the list of inbound sentinels and applies their {@code inspectInbound} method to the provided input.
     * Any {@link InboundSentinelException} encountered during inspection is caught and suppressed.
     * </p>
     *
     * @param rawInput the raw input to inspect
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
     * Inspects the outbound output by invoking the inspection on each registered outbound sentinel.
     * <p>
     * Iterates through all outbound sentinels and calls their {@code inspectOutbound} method
     * with the provided output. Any {@code OutboundSentinelException} thrown during the inspection
     * is caught and suppressed.
     *
     * @param output the output to be inspected by outbound sentinels
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
