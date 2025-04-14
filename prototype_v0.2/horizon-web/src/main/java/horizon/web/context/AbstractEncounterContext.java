package horizon.web.context;

import horizon.core.constant.Scheme;
import horizon.core.context.EncounterContext;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.scanner.SentinelScanner;
import horizon.web.provider.HorizonRendezvousProvider;
import horizon.web.provider.RendezvousProviderLoader;

import java.util.Map;

public abstract class AbstractEncounterContext<I extends RawInput, O extends RawOutput> implements EncounterContext<I, O> {

    protected SentinelScanner<I, O> sentinelScanner;
    protected Map<Scheme, HorizonRendezvousProvider<I, O>> rendezvousProviderMap;

    public AbstractEncounterContext(SentinelScanner<I, O> sentinelScanner) {
        this.sentinelScanner = sentinelScanner;
        this.rendezvousProviderMap = RendezvousProviderLoader.loadProviders();;
    }
}