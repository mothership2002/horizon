package horizon.flow.rendezvous;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.constant.Scheme;
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
import java.util.concurrent.ExecutorService;

public class DefaultRendezvous<I extends RawInput, O extends RawOutput> extends AbstractProtocolRendezvous<I, O> {


    public DefaultRendezvous(AbstractProtocolNormalizer<I> normalizer, AbstractProtocolInterpreter interpreter,
                             AbstractConductorManager conductorManager, RawOutputBuilder<O> rawOutputBuilder,
                             AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner sentinelScanner, ExecutorService rendezvousExecutor) {
        super(normalizer, interpreter, conductorManager, rawOutputBuilder, shadowStage, scheme, sentinelScanner, rendezvousExecutor);
    }

    @Override
    public CompletableFuture<O> encounter(I rawInput) {
        return CompletableFuture.supplyAsync(() -> {
            preInspect(rawInput);
            NormalizedInput normalized = normalizer.normalize(rawInput);
            ParsedRequest parsed = interpreter.interpret(normalized);
            Object result = conductorManager.conduct(parsed);
            O output = rawOutputBuilder.build(result);
            postInspect(output);
            return output;
        }, rendezvousExecutor);
    }

    @Override
    public void addInboundSentinel(FlowSentinel.InboundSentinel<I> sentinel) {
        inboundSentinels.add(sentinel);
    }

    @Override
    public void addOutboundSentinel(FlowSentinel.OutboundSentinel<O> sentinel) {
        outboundSentinels.add(sentinel);
    }


}
