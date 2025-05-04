package demo;

import horizon.core.constant.Scheme;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.scanner.SentinelScanner;
import horizon.http.HttpInterpreter;
import horizon.http.HttpNormalizer;
import horizon.http.context.HttpEncounterContext;
import horizon.http.model.HttpInput;
import horizon.http.model.HttpOutput;
import horizon.web.rendezvous.RendezvousBuilder;

public class Main {
    public static void main(String[] args) {
        SentinelScanner<RawInput, RawOutput> auto = SentinelScanner.auto();
        new HttpEncounterContextImpl(auto);
    }

    public static class HttpEncounterContextImpl extends HttpEncounterContext {

        public HttpEncounterContextImpl(SentinelScanner<HttpInput, HttpOutput> sentinelScanner) {
            super(sentinelScanner);
        }

        @Override
        protected RendezvousBuilder<HttpInput, HttpOutput> rendezvousBuilder() {
            return RendezvousBuilder.getBuilder(sentinelScanner, rendezvousProviderMap)
                    .withScheme(Scheme.HTTP)
                    .withNormalizer(new HttpNormalizer())
                    .withInterpreter(new HttpInterpreter());
        }
    }
}