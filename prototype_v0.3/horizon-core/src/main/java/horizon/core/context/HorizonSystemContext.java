package horizon.core.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HorizonSystemContext {

    private final Map<String, HorizonRuntimeUnit> units = new HashMap<>();

    public void register(String scheme, HorizonRuntimeUnit unit) {
        units.put(scheme, unit);
    }

    public Optional<HorizonRuntimeUnit> resolve(String scheme) {
        return Optional.ofNullable(units.get(scheme));
    }
}
