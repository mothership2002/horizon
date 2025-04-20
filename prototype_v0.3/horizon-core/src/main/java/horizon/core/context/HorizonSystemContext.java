package horizon.core.context;

import horizon.core.constant.Scheme;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HorizonSystemContext represents the global registry of Horizon runtime units.
 * Each scheme (e.g. http, cli, ws) maps to a self-contained runtime configuration
 * that manages its own Rendezvous, Conductor, and Stage components.
 * 
 * This class is thread-safe and can be used concurrently from multiple threads.
 */
public class HorizonSystemContext {
    private static final Logger LOGGER = Logger.getLogger(HorizonSystemContext.class.getName());

    private final Map<Scheme, HorizonRuntimeUnit<?, ?, ?, ?, ?>> runtimeUnits = new ConcurrentHashMap<>();
    private boolean initialized = false;
    private boolean shutdown = false;

    /**
     * Initializes this system context.
     * This method should be called before using the context.
     * 
     * @return this system context
     * @throws IllegalStateException if the context is already initialized or has been shut down
     */
    public synchronized HorizonSystemContext initialize() {
        if (initialized) {
            throw new IllegalStateException("HorizonSystemContext is already initialized");
        }
        if (shutdown) {
            throw new IllegalStateException("HorizonSystemContext has been shut down");
        }

        LOGGER.info("Initializing HorizonSystemContext");
        initialized = true;
        return this;
    }

    /**
     * Shuts down this system context.
     * This method should be called when the context is no longer needed.
     * 
     * @throws IllegalStateException if the context is not initialized or has already been shut down
     */
    public synchronized void shutdown() {
        if (!initialized) {
            throw new IllegalStateException("HorizonSystemContext is not initialized");
        }
        if (shutdown) {
            throw new IllegalStateException("HorizonSystemContext has already been shut down");
        }

        LOGGER.info("Shutting down HorizonSystemContext");
        runtimeUnits.clear();
        shutdown = true;
    }

    /**
     * Registers a runtime unit for the specified scheme.
     * If a unit is already registered for the scheme, it will be replaced.
     *
     * @param <I> the type of raw input
     * @param <N> the type of normalized input
     * @param <K> the type of intent key
     * @param <P> the type of intent payload
     * @param <O> the type of raw output
     * @param scheme the scheme to register the unit for
     * @param unit the runtime unit to register
     * @throws NullPointerException if scheme or unit is null
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    public <I extends RawInput, N, K, P, O extends RawOutput> void registerUnit(Scheme scheme, HorizonRuntimeUnit<I, N, K, P, O> unit) {
        checkInitialized();
        Objects.requireNonNull(scheme, "scheme must not be null");
        Objects.requireNonNull(unit, "unit must not be null");

        LOGGER.info("Registering runtime unit for scheme: " + scheme);
        runtimeUnits.put(scheme, unit);
    }

    /**
     * Resolves the runtime unit for the specified scheme.
     *
     * @param <I> the type of raw input
     * @param <N> the type of normalized input
     * @param <K> the type of intent key
     * @param <P> the type of intent payload
     * @param <O> the type of raw output
     * @param scheme the scheme to resolve the unit for
     * @return an Optional containing the runtime unit, or an empty Optional if no unit is registered for the scheme
     * @throws NullPointerException if scheme is null
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    @SuppressWarnings("unchecked")
    public <I extends RawInput, N, K, P, O extends RawOutput> Optional<HorizonRuntimeUnit<I, N, K, P, O>> resolveUnit(Scheme scheme) {
        checkInitialized();
        Objects.requireNonNull(scheme, "scheme must not be null");

        LOGGER.fine("Resolving runtime unit for scheme: " + scheme);
        return Optional.ofNullable((HorizonRuntimeUnit<I, N, K, P, O>) runtimeUnits.get(scheme));
    }

    /**
     * Unregisters the runtime unit for the specified scheme.
     *
     * @param scheme the scheme to unregister the unit for
     * @return true if a unit was unregistered, false if no unit was registered for the scheme
     * @throws NullPointerException if scheme is null
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    public boolean unregisterUnit(Scheme scheme) {
        checkInitialized();
        Objects.requireNonNull(scheme, "scheme must not be null");

        LOGGER.info("Unregistering runtime unit for scheme: " + scheme);
        return runtimeUnits.remove(scheme) != null;
    }

    /**
     * Returns a set of all registered schemes.
     *
     * @return an unmodifiable set of all registered schemes
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    public Set<Scheme> getRegisteredSchemes() {
        checkInitialized();
        return Collections.unmodifiableSet(runtimeUnits.keySet());
    }

    /**
     * Returns the number of registered runtime units.
     *
     * @return the number of registered runtime units
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    public int getUnitCount() {
        checkInitialized();
        return runtimeUnits.size();
    }

    /**
     * Returns whether this system context is initialized.
     *
     * @return true if this system context is initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns whether this system context is shut down.
     *
     * @return true if this system context is shut down, false otherwise
     */
    public boolean isShutdown() {
        return shutdown;
    }

    /**
     * Checks if this system context is initialized and not shut down.
     *
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("HorizonSystemContext is not initialized");
        }
        if (shutdown) {
            throw new IllegalStateException("HorizonSystemContext has been shut down");
        }
    }
}
