package horizon.core.context;

import horizon.core.constant.Scheme;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * HorizonSystemContext represents the global registry of Horizon runtime units.
 * Each scheme (e.g. http, cli, ws) maps to a self-contained runtime configuration
 * that manages its own Rendezvous, Conductor, and Stage components.
 */
public class HorizonSystemContext {

    private final Map<Scheme, HorizonRuntimeUnit> runtimeUnits = new HashMap<>();

    public void registerUnit(Scheme scheme, HorizonRuntimeUnit unit) {
        runtimeUnits.put(scheme, unit);
    }

    public Optional<HorizonRuntimeUnit> resolveUnit(Scheme scheme) {
        return Optional.ofNullable(runtimeUnits.get(scheme));
    }
}
