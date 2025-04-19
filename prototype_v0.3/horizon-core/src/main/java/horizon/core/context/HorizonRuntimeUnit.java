package horizon.core.context;

import horizon.core.rendezvous.RendezvousDescriptor;

import java.util.HashMap;
import java.util.Optional;

public class HorizonRuntimeUnit {

    private final RendezvousDescriptor<?, ?> rendezvousDescriptor;
    private final Map<String, Conductor<?>> conductorMap = new HashMap<>();
    private final Map<String, StageHandler<?>> centralStageMap = new HashMap<>();
    private final Map<String, StageHandler<?>> shallowStageMap = new HashMap<>();

    public HorizonRuntimeUnit(RendezvousDescriptor<?, ?> rendezvousDescriptor) {
        this.rendezvousDescriptor = rendezvousDescriptor;
    }

    public RendezvousDescriptor<?, ?> getRendezvousDescriptor() {
        return rendezvousDescriptor;
    }

    public void registerConductor(String intentKey, Conductor<?> conductor) {
        conductorMap.put(intentKey, conductor);
    }

    public void registerCentralStage(String commandKey, StageHandler<?> handler) {
        centralStageMap.put(commandKey, handler);
    }

    public void registerShallowStage(String commandKey, StageHandler<?> handler) {
        shallowStageMap.put(commandKey, handler);
    }

    public Optional<Conductor<?>> getConductor(String intentKey) {
        return Optional.ofNullable(conductorMap.get(intentKey));
    }

    public Optional<StageHandler<?>> getCentralStage(String commandKey) {
        return Optional.ofNullable(centralStageMap.get(commandKey));
    }

    public Optional<StageHandler<?>> getShallowStage(String commandKey) {
        return Optional.ofNullable(shallowStageMap.get(commandKey));
    }
}
