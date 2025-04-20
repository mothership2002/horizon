package horizon.core.context;

import horizon.core.conductor.Conductor;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.RendezvousDescriptor;
import horizon.core.stage.StageHandler;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A runtime unit for the Horizon framework that manages rendezvous, conductors, and stage handlers.
 * This class is thread-safe and can be used concurrently from multiple threads.
 *
 * @param <I> the type of raw input
 * @param <N> the type of normalized input
 * @param <K> the type of intent key
 * @param <P> the type of intent payload
 * @param <O> the type of raw output
 */
public class HorizonRuntimeUnit<I extends RawInput, N, K, P, O extends RawOutput> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HorizonRuntimeUnit.class);

    private final RendezvousDescriptor<I, N, K, P, O> rendezvousDescriptor;
    private final Map<String, Conductor<P>> conductorMap = new ConcurrentHashMap<>();
    private final Map<String, StageHandler> centralStageMap = new ConcurrentHashMap<>();
    private final Map<String, StageHandler> shallowStageMap = new ConcurrentHashMap<>();

    /**
     * Creates a new runtime unit with the specified rendezvous descriptor.
     *
     * @param rendezvousDescriptor the descriptor for the rendezvous
     * @throws NullPointerException if rendezvousDescriptor is null
     */
    public HorizonRuntimeUnit(RendezvousDescriptor<I, N, K, P, O> rendezvousDescriptor) {
        this.rendezvousDescriptor = Objects.requireNonNull(rendezvousDescriptor, "rendezvousDescriptor must not be null");
    }

    /**
     * Private constructor used by the builder.
     */
    private HorizonRuntimeUnit(Builder<I, N, K, P, O> builder) {
        this.rendezvousDescriptor = Objects.requireNonNull(builder.rendezvousDescriptor, "rendezvousDescriptor must not be null");

        // Copy all registered components from the builder
        if (builder.conductors != null) {
            this.conductorMap.putAll(builder.conductors);
        }
        if (builder.centralStages != null) {
            this.centralStageMap.putAll(builder.centralStages);
        }
        if (builder.shallowStages != null) {
            this.shallowStageMap.putAll(builder.shallowStages);
        }
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
     * If a conductor is already registered for the key, it will be replaced.
     *
     * @param intentKey the intent key
     * @param conductor the conductor to register
     * @throws NullPointerException if intentKey or conductor is null
     */
    public void registerConductor(String intentKey, Conductor<P> conductor) {
        Objects.requireNonNull(intentKey, "intentKey must not be null");
        Objects.requireNonNull(conductor, "conductor must not be null");

        LOGGER.debug("Registering conductor for intent key: {}", intentKey);
        conductorMap.put(intentKey, conductor);
    }

    /**
     * Returns the conductor for the specified intent key.
     *
     * @param intentKey the intent key
     * @return an Optional containing the conductor, or an empty Optional if no conductor is registered for the key
     * @throws NullPointerException if intentKey is null
     */
    public Optional<Conductor<P>> getConductor(String intentKey) {
        Objects.requireNonNull(intentKey, "intentKey must not be null");
        return Optional.ofNullable(conductorMap.get(intentKey));
    }

    /**
     * Unregisters the conductor for the specified intent key.
     *
     * @param intentKey the intent key
     * @return true if a conductor was unregistered, false if no conductor was registered for the key
     * @throws NullPointerException if intentKey is null
     */
    public boolean unregisterConductor(String intentKey) {
        Objects.requireNonNull(intentKey, "intentKey must not be null");

        LOGGER.debug("Unregistering conductor for intent key: {}", intentKey);
        return conductorMap.remove(intentKey) != null;
    }

    /**
     * Returns a set of all registered intent keys for conductors.
     *
     * @return an unmodifiable set of all registered intent keys
     */
    public Set<String> getRegisteredIntentKeys() {
        return Collections.unmodifiableSet(conductorMap.keySet());
    }

    /**
     * Returns the number of registered conductors.
     *
     * @return the number of registered conductors
     */
    public int getConductorCount() {
        return conductorMap.size();
    }

    /**
     * Registers a central stage handler for the specified command key.
     * If a handler is already registered for the key, it will be replaced.
     *
     * @param commandKey the command key
     * @param handler the stage handler to register
     * @throws NullPointerException if commandKey or handler is null
     */
    public void registerCentralStage(String commandKey, StageHandler handler) {
        Objects.requireNonNull(commandKey, "commandKey must not be null");
        Objects.requireNonNull(handler, "handler must not be null");

        LOGGER.debug("Registering central stage for command key: {}", commandKey);
        centralStageMap.put(commandKey, handler);
    }

    /**
     * Unregisters the central stage handler for the specified command key.
     *
     * @param commandKey the command key
     * @return true if a handler was unregistered, false if no handler was registered for the key
     * @throws NullPointerException if commandKey is null
     */
    public boolean unregisterCentralStage(String commandKey) {
        Objects.requireNonNull(commandKey, "commandKey must not be null");

        LOGGER.debug("Unregistering central stage for command key: {}", commandKey);
        return centralStageMap.remove(commandKey) != null;
    }

    /**
     * Returns a set of all registered command keys for central stage handlers.
     *
     * @return an unmodifiable set of all registered command keys
     */
    public Set<String> getRegisteredCentralStageKeys() {
        return Collections.unmodifiableSet(centralStageMap.keySet());
    }

    /**
     * Returns the number of registered central stage handlers.
     *
     * @return the number of registered central stage handlers
     */
    public int getCentralStageCount() {
        return centralStageMap.size();
    }

    /**
     * Registers a shallow stage handler for the specified command key.
     * If a handler is already registered for the key, it will be replaced.
     *
     * @param commandKey the command key
     * @param handler the stage handler to register
     * @throws NullPointerException if commandKey or handler is null
     */
    public void registerShallowStage(String commandKey, StageHandler handler) {
        Objects.requireNonNull(commandKey, "commandKey must not be null");
        Objects.requireNonNull(handler, "handler must not be null");

        LOGGER.debug("Registering shallow stage for command key: {}", commandKey);
        shallowStageMap.put(commandKey, handler);
    }

    /**
     * Unregisters the shallow stage handler for the specified command key.
     *
     * @param commandKey the command key
     * @return true if a handler was unregistered, false if no handler was registered for the key
     * @throws NullPointerException if commandKey is null
     */
    public boolean unregisterShallowStage(String commandKey) {
        Objects.requireNonNull(commandKey, "commandKey must not be null");

        LOGGER.debug("Unregistering shallow stage for command key: {}", commandKey);
        return shallowStageMap.remove(commandKey) != null;
    }

    /**
     * Returns a set of all registered command keys for shallow stage handlers.
     *
     * @return an unmodifiable set of all registered command keys
     */
    public Set<String> getRegisteredShallowStageKeys() {
        return Collections.unmodifiableSet(shallowStageMap.keySet());
    }

    /**
     * Returns the number of registered shallow stage handlers.
     *
     * @return the number of registered shallow stage handlers
     */
    public int getShallowStageCount() {
        return shallowStageMap.size();
    }

    /**
     * Returns the central stage handler for the specified command key.
     *
     * @param commandKey the command key
     * @return an Optional containing the stage handler, or an empty Optional if no handler is registered for the key
     * @throws NullPointerException if commandKey is null
     */
    public Optional<StageHandler> getCentralStage(String commandKey) {
        Objects.requireNonNull(commandKey, "commandKey must not be null");
        return Optional.ofNullable(centralStageMap.get(commandKey));
    }

    /**
     * Returns the shallow stage handler for the specified command key.
     *
     * @param commandKey the command key
     * @return an Optional containing the stage handler, or an empty Optional if no handler is registered for the key
     * @throws NullPointerException if commandKey is null
     */
    public Optional<StageHandler> getShallowStage(String commandKey) {
        Objects.requireNonNull(commandKey, "commandKey must not be null");
        return Optional.ofNullable(shallowStageMap.get(commandKey));
    }

    /**
     * Returns a new builder for creating HorizonRuntimeUnit instances.
     *
     * @param <I> the type of raw input
     * @param <N> the type of normalized input
     * @param <K> the type of intent key
     * @param <P> the type of intent payload
     * @param <O> the type of raw output
     * @param rendezvousDescriptor the descriptor for the rendezvous
     * @return a new builder
     * @throws NullPointerException if rendezvousDescriptor is null
     */
    public static <I extends RawInput, N, K, P, O extends RawOutput> Builder<I, N, K, P, O> builder(
            RendezvousDescriptor<I, N, K, P, O> rendezvousDescriptor) {
        return new Builder<>(rendezvousDescriptor);
    }

    /**
     * A builder for creating HorizonRuntimeUnit instances.
     *
     * @param <I> the type of raw input
     * @param <N> the type of normalized input
     * @param <K> the type of intent key
     * @param <P> the type of intent payload
     * @param <O> the type of raw output
     */
    public static class Builder<I extends RawInput, N, K, P, O extends RawOutput> {
        private final RendezvousDescriptor<I, N, K, P, O> rendezvousDescriptor;
        private Map<String, Conductor<P>> conductors;
        private Map<String, StageHandler> centralStages;
        private Map<String, StageHandler> shallowStages;

        /**
         * Creates a new builder with the specified rendezvous descriptor.
         *
         * @param rendezvousDescriptor the descriptor for the rendezvous
         * @throws NullPointerException if rendezvousDescriptor is null
         */
        public Builder(RendezvousDescriptor<I, N, K, P, O> rendezvousDescriptor) {
            this.rendezvousDescriptor = Objects.requireNonNull(rendezvousDescriptor, "rendezvousDescriptor must not be null");
        }

        /**
         * Adds a conductor to the runtime unit being built.
         *
         * @param intentKey the intent key
         * @param conductor the conductor to add
         * @return this builder
         * @throws NullPointerException if intentKey or conductor is null
         */
        public Builder<I, N, K, P, O> addConductor(String intentKey, Conductor<P> conductor) {
            Objects.requireNonNull(intentKey, "intentKey must not be null");
            Objects.requireNonNull(conductor, "conductor must not be null");

            if (conductors == null) {
                conductors = new ConcurrentHashMap<>();
            }
            conductors.put(intentKey, conductor);
            return this;
        }

        /**
         * Adds a central stage handler to the runtime unit being built.
         *
         * @param commandKey the command key
         * @param handler the stage handler to add
         * @return this builder
         * @throws NullPointerException if commandKey or handler is null
         */
        public Builder<I, N, K, P, O> addCentralStage(String commandKey, StageHandler handler) {
            Objects.requireNonNull(commandKey, "commandKey must not be null");
            Objects.requireNonNull(handler, "handler must not be null");

            if (centralStages == null) {
                centralStages = new ConcurrentHashMap<>();
            }
            centralStages.put(commandKey, handler);
            return this;
        }

        /**
         * Adds a shallow stage handler to the runtime unit being built.
         *
         * @param commandKey the command key
         * @param handler the stage handler to add
         * @return this builder
         * @throws NullPointerException if commandKey or handler is null
         */
        public Builder<I, N, K, P, O> addShallowStage(String commandKey, StageHandler handler) {
            Objects.requireNonNull(commandKey, "commandKey must not be null");
            Objects.requireNonNull(handler, "handler must not be null");

            if (shallowStages == null) {
                shallowStages = new ConcurrentHashMap<>();
            }
            shallowStages.put(commandKey, handler);
            return this;
        }

        /**
         * Builds a new HorizonRuntimeUnit with the values set in this builder.
         *
         * @return a new HorizonRuntimeUnit
         */
        public HorizonRuntimeUnit<I, N, K, P, O> build() {
            return new HorizonRuntimeUnit<>(this);
        }
    }
}
