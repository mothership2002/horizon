package horizon.core.context;

import horizon.core.conductor.Conductor;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.RendezvousDescriptor;
import horizon.core.stage.StageHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A runtime unit for the Horizon framework that manages rendezvous, conductors, and stage handlers.
 *
 * @param <I> the type of raw input
 * @param <N> the type of normalized input
 * @param <K> the type of intent key
 * @param <P> the type of intent payload
 * @param <O> the type of raw output
 */
public class HorizonRuntimeUnit<I extends RawInput, N, K, P, O extends RawOutput> {

    private final RendezvousDescriptor<I, N, K, P, O> rendezvousDescriptor;
    private final Map<String, Conductor<P>> conductorMap = new HashMap<>();
    private final Map<String, StageHandler> centralStageMap = new HashMap<>();
    private final Map<String, StageHandler> shallowStageMap = new HashMap<>();

    /**
     * Creates a new runtime unit with the specified rendezvous descriptor.
     *
     * @param rendezvousDescriptor the descriptor for the rendezvous
     * @throws NullPointerException if rendezvousDescriptor is null
     */
    public HorizonRuntimeUnit(RendezvousDescriptor<I, N, K, P, O> rendezvousDescriptor) {
        this.rendezvousDescriptor = java.util.Objects.requireNonNull(rendezvousDescriptor, "rendezvousDescriptor must not be null");
    }

    /**
     * Returns the rendezvous descriptor for this runtime unit.
     *
     * @return the rendezvous descriptor
     */
    public RendezvousDescriptor<I, N, K, P, O> getRendezvousDescriptor() {
        return rendezvousDescriptor;
    }

    /**
     * Registers a conductor for the specified intent key.
     *
     * @param intentKey the intent key
     * @param conductor the conductor to register
     * @throws NullPointerException if intentKey or conductor is null
     */
    public void registerConductor(String intentKey, Conductor<P> conductor) {
        java.util.Objects.requireNonNull(intentKey, "intentKey must not be null");
        java.util.Objects.requireNonNull(conductor, "conductor must not be null");
        conductorMap.put(intentKey, conductor);
    }

    /**
     * Returns the conductor for the specified intent key.
     *
     * @param intentKey the intent key
     * @return an Optional containing the conductor, or an empty Optional if no conductor is registered for the key
     */
    public Optional<Conductor<P>> getConductor(String intentKey) {
        return Optional.ofNullable(conductorMap.get(intentKey));
    }

    /**
     * Registers a central stage handler for the specified command key.
     *
     * @param commandKey the command key
     * @param handler the stage handler to register
     * @throws NullPointerException if commandKey or handler is null
     */
    public void registerCentralStage(String commandKey, StageHandler handler) {
        java.util.Objects.requireNonNull(commandKey, "commandKey must not be null");
        java.util.Objects.requireNonNull(handler, "handler must not be null");
        centralStageMap.put(commandKey, handler);
    }

    /**
     * Registers a shallow stage handler for the specified command key.
     *
     * @param commandKey the command key
     * @param handler the stage handler to register
     * @throws NullPointerException if commandKey or handler is null
     */
    public void registerShallowStage(String commandKey, StageHandler handler) {
        java.util.Objects.requireNonNull(commandKey, "commandKey must not be null");
        java.util.Objects.requireNonNull(handler, "handler must not be null");
        shallowStageMap.put(commandKey, handler);
    }

    /**
     * Returns the central stage handler for the specified command key.
     *
     * @param commandKey the command key
     * @return an Optional containing the stage handler, or an empty Optional if no handler is registered for the key
     */
    public Optional<StageHandler> getCentralStage(String commandKey) {
        return Optional.ofNullable(centralStageMap.get(commandKey));
    }

    /**
     * Returns the shallow stage handler for the specified command key.
     *
     * @param commandKey the command key
     * @return an Optional containing the stage handler, or an empty Optional if no handler is registered for the key
     */
    public Optional<StageHandler> getShallowStage(String commandKey) {
        return Optional.ofNullable(shallowStageMap.get(commandKey));
    }
}
