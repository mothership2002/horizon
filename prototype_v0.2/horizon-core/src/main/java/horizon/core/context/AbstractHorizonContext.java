package horizon.core.context;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.scanner.SentinelScanner;

interface HorizonContext<I extends RawInput, O extends RawOutput> {

}

public class AbstractHorizonContext<I extends RawInput, O extends RawOutput> implements HorizonContext<I, O> {

    private final SentinelScanner<I, O> sentinelScanner;
    private final EncounterContext<I, O> encounterContext;
    private final ManifestContext manifestContext;
    private final CommitContext commitContext;

    public AbstractHorizonContext(SentinelScanner<I, O> sentinelScanner,
                                  EncounterContext<I, O> encounterContext,
                                  ManifestContext manifestContext,
                                  CommitContext commitContext) {

        this.sentinelScanner = sentinelScanner;
        this.encounterContext = encounterContext;
        this.manifestContext = manifestContext;
        this.commitContext = commitContext;
    }
}

