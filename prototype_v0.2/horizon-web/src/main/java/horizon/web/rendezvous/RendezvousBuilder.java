package horizon.web.rendezvous;

import horizon.core.constant.Scheme;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.AbstractRendezvous;
import horizon.core.rendezvous.interpreter.AbstractInterpreter;
import horizon.core.rendezvous.normalizer.Normalizer;
import horizon.core.rendezvous.sentinel.InboundSentinel;
import horizon.core.rendezvous.sentinel.OutboundSentinel;
import horizon.core.scanner.SentinelScanner;
import horizon.core.util.Assert;
import horizon.web.provider.HorizonRendezvousProvider;

import java.util.List;
import java.util.Map;


public class RendezvousBuilder<I extends RawInput, O extends RawOutput>  implements SchemeStage<I, O>, NormalizerStage<I, O>, InterpreterStage<I, O>, BuildStage<I, O> {

    private final SentinelScanner<I, O> sentinelScanner;
    private final Map<Scheme, HorizonRendezvousProvider<I, O>> rendezvousProviders;

    private Scheme scheme;
    private Normalizer normalizer;
    private AbstractInterpreter interpreter;
    private List<InboundSentinel<I>> inboundSentinels;
    private List<OutboundSentinel<O>> outboundSentinels;

    public RendezvousBuilder(SentinelScanner<I, O> sentinelScanner, Map<Scheme, HorizonRendezvousProvider<I, O>> rendezvousProviders) {
        this.sentinelScanner = sentinelScanner;
        this.rendezvousProviders = rendezvousProviders;
    }

    public static <I extends RawInput, O extends RawOutput> RendezvousBuilder<I, O> getBuilder(
            SentinelScanner<I, O> sentinelScanner,
            Map<Scheme, HorizonRendezvousProvider<I, O>> rendezvousProviders) {
        return new RendezvousBuilder<>(sentinelScanner, rendezvousProviders);
    }

    @Override
    public RendezvousBuilder<I, O> withScheme(Scheme scheme) {
        this.scheme = scheme;
        return this;
    }

    @Override
    public RendezvousBuilder<I, O> withNormalizer(Normalizer normalizer) {
        this.normalizer = normalizer;
        return this;
    }

    @Override
    public RendezvousBuilder<I, O> withInterpreter(AbstractInterpreter interpreter) {
        this.interpreter = interpreter;
        return this;
    }

    @Override
    public AbstractRendezvous<I, O> build() {
        Assert.notNull(this.scheme, "Scheme must be not null");
        Assert.notNull(this.normalizer, "Normalizer must be not null");
        Assert.notNull(this.interpreter, "Interpreter must be not null");

        HorizonRendezvousProvider<I, O> rendezvousProvider = getIoHorizonRendezvousProvider();
        inboundSentinels = sentinelScanner.getInboundSentinels(this.scheme.name());
        outboundSentinels = sentinelScanner.getOutboundSentinels(this.scheme.name());
        return rendezvousProvider.build(this);
    }

    private HorizonRendezvousProvider<I, O> getIoHorizonRendezvousProvider() {
        HorizonRendezvousProvider<I, O> rendezvousProvider = rendezvousProviders.get(this.scheme);
        Assert.notNull(rendezvousProvider, "rendezvousProvider must be not null");
        return rendezvousProvider;
    }

    public Scheme getScheme() {
        return scheme;
    }

    public Normalizer getNormalizer() {
        return normalizer;
    }

    public AbstractInterpreter getInterpreter() {
        return interpreter;
    }

    public List<InboundSentinel<I>> getInboundSentinels() {
        return inboundSentinels;
    }

    public List<OutboundSentinel<O>> getOutboundSentinels() {
        return outboundSentinels;
    }
}


