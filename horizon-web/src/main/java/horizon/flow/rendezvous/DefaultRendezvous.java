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
     * Constructs a new DefaultRendezvous instance with the specified processing components.
     *
     * <p>This constructor initializes the DefaultRendezvous by delegating to its superclass
     * with the provided dependencies that are used for normalizing inputs, interpreting data,
     * managing conductors, building raw outputs, and applying additional shadow processing.
     * It also configures the processing scheme and integrates a sentinel scanner for validating
     * and managing flow sentinels.</p>
     *
     * @param normalizer     the normalizer used to preprocess raw inputs
     * @param interpreter    the interpreter that converts normalized inputs into executable requests
     * @param conductorManager the manager that coordinates protocol conduction processes
     * @param rawOutputBuilder the builder responsible for assembling raw outputs after processing
     * @param shadowStage    the stage for applying supplementary processing to the data flow
     * @param scheme         the scheme that defines the processing configuration
     * @param sentinelScanner the scanner used for validating and managing flow sentinels
     */
    public DefaultRendezvous(AbstractProtocolNormalizer<T> normalizer, AbstractProtocolInterpreter interpreter,
                             AbstractConductorManager conductorManager, RawOutputBuilder<S> rawOutputBuilder, AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner sentinelScanner) {
        super(normalizer, interpreter, conductorManager, rawOutputBuilder, shadowStage, scheme, sentinelScanner);
    }

    /**
     * Processes the provided raw input and returns an asynchronous output.
     *
     * <p>The method begins by performing a pre-inspection on the raw input. It then normalizes the input data and interprets the resulting normalized data
     * to generate a parsed request. Finally, it asynchronously processes the parsed request to produce the final output.</p>
     *
     * @param rawInput the raw input data to be processed
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
     * Registers an inbound sentinel to monitor and control incoming data.
     *
     * <p>This method adds the specified inbound sentinel to the internal collection, allowing the
     * system to evaluate and enforce inbound data flow policies.</p>
     *
     * @param sentinel the inbound sentinel to be added
     */
    @Override
    public void addInboundSentinel(FlowSentinel.InboundSentinel<T> sentinel) {
        inboundSentinels.add(sentinel);
    }

    /**
     * Adds an outbound sentinel used to monitor or control the flow of outgoing data.
     *
     * @param sentinel the outbound sentinel to register
     */
    @Override
    public void addOutboundSentinel(FlowSentinel.OutboundSentinel<S> sentinel) {
        outboundSentinels.add(sentinel);
    }


}
