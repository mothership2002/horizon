package horizon.core.context;

import horizon.core.conductor.Conductor;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.RendezvousDescriptor;
import horizon.core.stage.StageHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HorizonRuntimeUnit<I extends RawInput, N, K, P, O extends RawOutput> {

    private final RendezvousDescriptor<I, N, K, P, O> rendezvousDescriptor;
    private final Map<K, Conductor<P>> conductorMap = new HashMap<>();
    private final Map<String, StageHandler> centralStageMap = new HashMap<>();
    private final Map<String, StageHandler> shallowStageMap = new HashMap<>();

    public HorizonRuntimeUnit(RendezvousDescriptor<I, N, K, P, O> rendezvousDescriptor) {
        this.rendezvousDescriptor = rendezvousDescriptor;
    }

    public RendezvousDescriptor<I, N, K, P, O> getRendezvousDescriptor() {
        return rendezvousDescriptor;
    }

    public void registerConductor(K intentKey, Conductor<P> conductor) {
        conductorMap.put(intentKey, conductor);
    }

    public Optional<Conductor<P>> getConductor(K intentKey) {
        return Optional.ofNullable(conductorMap.get(intentKey));
    }

    public void registerCentralStage(String commandKey, StageHandler handler) {
        centralStageMap.put(commandKey, handler);
    }

    public void registerShallowStage(String commandKey, StageHandler handler) {
        shallowStageMap.put(commandKey, handler);
    }

    public Optional<StageHandler> getCentralStage(String commandKey) {
        return Optional.ofNullable(centralStageMap.get(commandKey));
    }

    public Optional<StageHandler> getShallowStage(String commandKey) {
        return Optional.ofNullable(shallowStageMap.get(commandKey));
    }
}