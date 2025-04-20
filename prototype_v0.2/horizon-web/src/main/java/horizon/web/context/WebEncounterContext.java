package horizon.web.context;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.scanner.SentinelScanner;

public abstract class WebEncounterContext<I extends RawInput, O extends RawOutput> extends AbstractEncounterContext<I, O> {

    public WebEncounterContext(SentinelScanner<I, O> sentinelScanner) {
        super(sentinelScanner);
    }

}
