package horizon.http.provider;

import com.google.auto.service.AutoService;
import horizon.core.constant.Scheme;
import horizon.core.rendezvous.AbstractRendezvous;
import horizon.core.rendezvous.interpreter.AbstractInterpreter;
import horizon.core.rendezvous.normalizer.Normalizer;
import horizon.core.rendezvous.sentinel.InboundSentinel;
import horizon.core.rendezvous.sentinel.OutboundSentinel;
import horizon.http.model.HttpInput;
import horizon.http.model.HttpOutput;
import horizon.web.provider.HorizonRendezvousProvider;
import horizon.web.rendezvous.RendezvousBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@AutoService(HorizonRendezvousProvider.class)
public class HttpRendezvousProvider implements HorizonRendezvousProvider<HttpInput, HttpOutput> {

    @Override
    public AbstractRendezvous<HttpInput, HttpOutput> build(RendezvousBuilder<HttpInput, HttpOutput> builder) {
        return new HttpRendezvous(builder.getInboundSentinels(),
                builder.getOutboundSentinels(),
                builder.getNormalizer(),
                builder.getInterpreter());
    }

    @Override
    public Scheme getScheme() {
        return Scheme.HTTP;
    }

    public static class HttpRendezvous extends AbstractRendezvous<HttpInput, HttpOutput> {

        protected HttpRendezvous(List<InboundSentinel<HttpInput>> inboundSentinels,
                                 List<OutboundSentinel<HttpOutput>> outboundSentinels,
                                 Normalizer normalizer,
                                 AbstractInterpreter interpreter) {
            super(inboundSentinels, outboundSentinels, normalizer, interpreter);
        }

        @Override
        public CompletableFuture<HttpOutput> encounter(HttpInput rawInput) {
            return null;
        }
    }
}