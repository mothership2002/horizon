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
import java.util.ServiceLoader;

public class RendezvousBuilder<I extends RawInput, O extends RawOutput> {

    private final SentinelScanner<I, O> sentinelScanner;

    private Scheme scheme;
    private Normalizer normalizer;
    private AbstractInterpreter interpreter;
    private List<InboundSentinel<I>> inboundSentinels;
    private List<OutboundSentinel<O>> outboundSentinels;

    public RendezvousBuilder(SentinelScanner<I, O> sentinelScanner) {
        this.sentinelScanner = sentinelScanner;
    }

    public static <I extends RawInput, O extends RawOutput> RendezvousBuilder<I, O> getBuilder(SentinelScanner<I, O> sentinelScanner) {
        return new RendezvousBuilder<>(sentinelScanner);
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
        ServiceLoader<HorizonRendezvousProvider<I, O>> load = ServiceLoader.load(HorizonRendezvousProvider<>.class);
        inboundSentinels = sentinelScanner.getInboundSentinels(this.scheme.name());
        outboundSentinels = sentinelScanner.getOutboundSentinels(this.scheme.name());
        return
    }

}
