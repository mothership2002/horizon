package horizon.core.context;

import horizon.core.constant.Scheme;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * HorizonSystemContext represents the global registry of Horizon runtime units.
 * Each scheme (e.g. http, cli, ws) maps to a self-contained runtime configuration
 * that manages its own Rendezvous, Conductor, and Stage components.
 */
public class HorizonSystemContext {

    private final Map<Scheme, HorizonRuntimeUnit<?, ?, ?, ?, ?>> runtimeUnits = new HashMap<>();

    public <I extends RawInput, N, K, P, O extends RawOutput>
    void registerUnit(Scheme scheme, HorizonRuntimeUnit<I, N, K, P, O> unit) {
        runtimeUnits.put(scheme, unit);
    }

    @SuppressWarnings("unchecked")
    public <I extends RawInput, N, K, P, O extends RawOutput>
    Optional<HorizonRuntimeUnit<I, N, K, P, O>> resolveUnit(Scheme scheme) {
        return Optional.ofNullable((HorizonRuntimeUnit<I, N, K, P, O>) runtimeUnits.get(scheme));
    }
}