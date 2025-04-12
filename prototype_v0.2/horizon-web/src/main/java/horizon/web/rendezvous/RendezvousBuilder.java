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
import horizon.web.provider.HorizonRendezvousProvider;

import java.util.List;
import java.util.Map;

public class RendezvousBuilder<I extends RawInput, O extends RawOutput> {

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

    public RendezvousBuilder<I, O> withScheme(Scheme scheme) {
        this.scheme = scheme;
        return this;
    }

    public RendezvousBuilder<I, O> withNormalizer(Normalizer normalizer) {
        this.normalizer = normalizer;
        return this;
    }

    public RendezvousBuilder<I, O> withInterpreter(AbstractInterpreter interpreter) {
        this.interpreter = interpreter;
        return this;
    }

    public AbstractRendezvous<I, O> build() {
        inboundSentinels = sentinelScanner.getInboundSentinels(this.scheme.name());
        outboundSentinels = sentinelScanner.getOutboundSentinels(this.scheme.name());
        HorizonRendezvousProvider<I, O> rendezvousProvider = rendezvousProviders.get(this.scheme);
        return rendezvousProvider.build(this);
    }

}
