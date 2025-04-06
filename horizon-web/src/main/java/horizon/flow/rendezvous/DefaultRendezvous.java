package horizon.flow.rendezvous;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.constant.Scheme;
import horizon.core.flow.sentinel.AbstractInboundSentinel;
import horizon.core.flow.sentinel.AbstractOutboundSentinel;
import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.normalizer.NormalizedInput;
import horizon.core.flow.rendezvous.AbstractProtocolRendezvous;
import horizon.core.flow.sentinel.FlowSentinel;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.stage.AbstractShadowStage;
import horizon.core.util.SentinelScanner;

import java.util.concurrent.CompletableFuture;

public class DefaultRendezvous<T extends RawInput, S extends RawOutput> extends AbstractProtocolRendezvous<T, S> {


    /**
     * Constructs a DefaultRendezvous instance with the required protocol components.
     *
     * <p>This constructor initializes a DefaultRendezvous by delegating the provided dependencies 
     * to its superclass. These dependencies include a normalizer to process raw inputs, an interpreter 
     * to convert normalized data, a conductor manager to orchestrate protocol operations, a builder to 
     * create raw outputs, a shadow stage for auxiliary operations, a scheme for protocol configuration, 
     * and a sentinel scanner for detecting and managing sentinels.
     *
     * @param normalizer       the protocol normalizer for preprocessing raw inputs
     * @param interpreter      the interpreter that converts normalized inputs into parsed requests
     * @param conductorManager the manager that coordinates protocol conduct operations
     * @param rawOutputBuilder the builder that constructs raw outputs from processed data
     * @param shadowStage      the stage responsible for managing shadow operations
     * @param scheme           the configuration scheme used for the protocol
     * @param sentinelScanner  the scanner used to detect and manage sentinels
     */
    public DefaultRendezvous(AbstractProtocolNormalizer<T> normalizer, AbstractProtocolInterpreter interpreter,
                             AbstractConductorManager conductorManager, RawOutputBuilder<S> rawOutputBuilder, AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner sentinelScanner) {
        super(normalizer, interpreter, conductorManager, rawOutputBuilder, shadowStage, scheme, sentinelScanner);
    }

    /**
     * Asynchronously processes the raw input and returns the result.
     *
     * <p>This method performs a preliminary inspection of the raw input, then normalizes
     * and interprets it to create a parsed request. The result of further processing this
     * parsed request is supplied asynchronously via a CompletableFuture.
     *
     * @param rawInput the raw input to process
     * @return a CompletableFuture that completes with the processed output
     */
    @Override
    public CompletableFuture<S> encounter(T rawInput) {
        preInspect(rawInput);
        NormalizedInput normalized = normalizer.normalize(rawInput);
        ParsedRequest parsed = interpreter.interpret(normalized);
        return CompletableFuture.supplyAsync(() -> afterConduct(parsed));
    }

    /**
     * Adds the given inbound sentinel to the collection of inbound sentinels.
     *
     * @param sentinel the inbound sentinel to be added
     */
    @Override
    public void addInboundSentinel(FlowSentinel.InboundSentinel<T> sentinel) {
        inboundSentinels.add(sentinel);
    }

    /**
     * Adds an outbound sentinel to the collection.
     *
     * @param sentinel the outbound sentinel to add
     */
    @Override
    public void addOutboundSentinel(FlowSentinel.OutboundSentinel<S> sentinel) {
        outboundSentinels.add(sentinel);
    }


}
