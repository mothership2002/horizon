package horizon.flow.rendezvous;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.flow.centinel.FlowSentinelInterface;
import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.normalizer.NormalizedInput;
import horizon.core.flow.rendezvous.AbstractProtocolRendezvous;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.stage.AbstractShadowStage;

import java.util.concurrent.CompletableFuture;

public class DefaultRendezvous<T extends RawInput, S extends RawOutput> extends AbstractProtocolRendezvous<T, S> {


    public DefaultRendezvous(AbstractProtocolNormalizer<T> normalizer, AbstractProtocolInterpreter interpreter,
                             AbstractConductorManager conductorManager, RawOutputBuilder<S> rawOutputBuilder, AbstractShadowStage shadowStage) {
        super(normalizer, interpreter, conductorManager, rawOutputBuilder, shadowStage);
    }

    @Override
    public CompletableFuture<S> encounter(T rawInput) {
        preInspect(rawInput);
        NormalizedInput normalized = normalizer.normalize(rawInput);
        ParsedRequest parsed = interpreter.interpret(normalized);
        return CompletableFuture.supplyAsync(() -> afterConduct(parsed));
    }

    @Override
    public void addInboundSentinel(FlowSentinelInterface.InboundSentinel<T> sentinel) {
        inboundSentinels.add(sentinel);
    }

    @Override
    public void addOutboundSentinel(FlowSentinelInterface.OutboundSentinel<S> sentinel) {
        outboundSentinels.add(sentinel);
    }


}
