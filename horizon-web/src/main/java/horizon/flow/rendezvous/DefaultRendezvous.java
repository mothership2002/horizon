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
     * Constructs a DefaultRendezvous with the specified protocol components and configuration options.
     *
     * <p>This constructor initializes the underlying protocol by setting up the normalizer for raw input,
     * the interpreter for generating parsed requests, the conductor manager for handling protocol flow,
     * the raw output builder for constructing the response, and the shadow stage for additional processing.
     * It also configures the protocol using the provided scheme and manages sentinels with the sentinel scanner.</p>
     *
     * @param normalizer       transforms raw input into a normalized format
     * @param interpreter      converts normalized data into a parsed request
     * @param conductorManager manages protocol communication
     * @param rawOutputBuilder builds the output from processed data
     * @param shadowStage      handles operations in the shadow processing stage
     * @param scheme           provides protocol configuration options
     * @param sentinelScanner  scans and manages protocol sentinels
     */
    public DefaultRendezvous(AbstractProtocolNormalizer<T> normalizer, AbstractProtocolInterpreter interpreter,
                             AbstractConductorManager conductorManager, RawOutputBuilder<S> rawOutputBuilder, AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner sentinelScanner) {
        super(normalizer, interpreter, conductorManager, rawOutputBuilder, shadowStage, scheme, sentinelScanner);
    }

    /**
     * Processes the provided raw input by performing a preliminary inspection, normalizing the input, interpreting the normalized data,
     * and asynchronously returning the processed result.
     *
     * @param rawInput the raw input to be processed
     * @return a CompletableFuture that asynchronously supplies the processed output
     */
    @Override
    public CompletableFuture<S> encounter(T rawInput) {
        preInspect(rawInput);
        NormalizedInput normalized = normalizer.normalize(rawInput);
        ParsedRequest parsed = interpreter.interpret(normalized);
        return CompletableFuture.supplyAsync(() -> afterConduct(parsed));
    }

    /**
     * Adds an inbound sentinel to the collection of sentinels monitoring incoming data.
     *
     * @param sentinel the inbound sentinel to add
     */
    @Override
    public void addInboundSentinel(FlowSentinel.InboundSentinel<T> sentinel) {
        inboundSentinels.add(sentinel);
    }

    /**
     * Adds an outbound sentinel to the rendezvous for processing outgoing responses.
     *
     * @param sentinel the outbound sentinel to handle or modify outbound flow events
     */
    @Override
    public void addOutboundSentinel(FlowSentinel.OutboundSentinel<S> sentinel) {
        outboundSentinels.add(sentinel);
    }


}
