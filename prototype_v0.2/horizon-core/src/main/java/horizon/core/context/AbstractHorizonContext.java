package horizon.core.context;

import horizon.core.constant.Scheme;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.scanner.SentinelScanner;

import java.util.Map;

public class AbstractHorizonContext<I extends RawInput, O extends RawOutput> implements HorizonContext<I, O> {

    private final SentinelScanner<I, O> sentinelScanner;

    public AbstractHorizonContext() {
        this.sentinelScanner = SentinelScanner.auto();

    }
}
