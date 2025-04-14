package horizon.http.context;

import horizon.core.constant.Scheme;
import horizon.core.rendezvous.AbstractRendezvous;
import horizon.core.scanner.SentinelScanner;
import horizon.http.model.HttpInput;
import horizon.http.model.HttpOutput;
import horizon.web.context.WebEncounterContext;
import horizon.web.provider.HorizonRendezvousProvider;
import horizon.web.rendezvous.RendezvousBuilder;

public abstract class HttpEncounterContext extends WebEncounterContext<HttpInput, HttpOutput> {

    public HttpEncounterContext(SentinelScanner<HttpInput, HttpOutput> sentinelScanner) {
        super(sentinelScanner);
    }

    @Override
    public AbstractRendezvous<HttpInput, HttpOutput> rendezvous() {
        HorizonRendezvousProvider<HttpInput, HttpOutput> provider = rendezvousProviderMap.get(Scheme.HTTP);
        return provider.build(rendezvousBuilder());
    }

    /**
     * This method must be implemented by subclasses to provide a configured `RendezvousBuilder`.
     * Subclasses should specify the necessary Scheme, Normalizer, and Interpreter here.
     * Example:
     * - return RendezvousBuilder.getBuilder(scanner, providerMap)
     * .withScheme(Scheme.HTTP)
     * .withNormalizer(new HttpNormalizer())
     * .withInterpreter(new HttpInterpreter());
     */
    protected abstract RendezvousBuilder<HttpInput, HttpOutput> rendezvousBuilder();
}
