package horizon.core.context;

import horizon.core.constant.Scheme;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.Foyer;
import horizon.core.rendezvous.protocol.ProtocolAdapter;
import horizon.core.rendezvous.protocol.ProtocolFoyer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<Scheme, Foyer<?>> foyers = new ConcurrentHashMap<>();
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

        // Shutdown all foyers
        for (Foyer<?> foyer : foyers.values()) {
            try {
                foyer.shutdown();
            } catch (Exception e) {
                LOGGER.warning("Error shutting down foyer: " + e.getMessage());
            }
        }
        foyers.clear();

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
     * Registers a foyer for the specified scheme.
     * If a foyer is already registered for the scheme, it will be replaced.
     *
     * @param <I> the type of raw input
     * @param scheme the scheme to register the foyer for
     * @param foyer the foyer to register
     * @throws NullPointerException if scheme or foyer is null
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    public <I extends RawInput> void registerFoyer(Scheme scheme, Foyer<I> foyer) {
        checkInitialized();
        Objects.requireNonNull(scheme, "scheme must not be null");
        Objects.requireNonNull(foyer, "foyer must not be null");

        LOGGER.info("Registering foyer for scheme: " + scheme);
        foyers.put(scheme, foyer);

        // Initialize the foyer
        foyer.initialize();
    }

    /**
     * Resolves the foyer for the specified scheme.
     *
     * @param <I> the type of raw input
     * @param scheme the scheme to resolve the foyer for
     * @return an Optional containing the foyer, or an empty Optional if no foyer is registered for the scheme
     * @throws NullPointerException if scheme is null
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    @SuppressWarnings("unchecked")
    public <I extends RawInput> Optional<Foyer<I>> resolveFoyer(Scheme scheme) {
        checkInitialized();
        Objects.requireNonNull(scheme, "scheme must not be null");

        LOGGER.fine("Resolving foyer for scheme: " + scheme);
        return Optional.ofNullable((Foyer<I>) foyers.get(scheme));
    }

    /**
     * Unregisters the foyer for the specified scheme.
     *
     * @param scheme the scheme to unregister the foyer for
     * @return true if a foyer was unregistered, false if no foyer was registered for the scheme
     * @throws NullPointerException if scheme is null
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    public boolean unregisterFoyer(Scheme scheme) {
        checkInitialized();
        Objects.requireNonNull(scheme, "scheme must not be null");

        LOGGER.info("Unregistering foyer for scheme: " + scheme);
        Foyer<?> foyer = foyers.remove(scheme);
        if (foyer != null) {
            foyer.shutdown();
            return true;
        }
        return false;
    }

    /**
     * Returns a set of all registered schemes for foyers.
     *
     * @return an unmodifiable set of all registered schemes
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    public Set<Scheme> getRegisteredFoyerSchemes() {
        checkInitialized();
        return Collections.unmodifiableSet(foyers.keySet());
    }

    /**
     * Returns the number of registered foyers.
     *
     * @return the number of registered foyers
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    public int getFoyerCount() {
        checkInitialized();
        return foyers.size();
    }

    /**
     * Creates and registers a protocol foyer for the specified scheme.
     * This is a generic method that can create any type of protocol foyer.
     *
     * @param <I> the type of raw input
     * @param <O> the type of raw output
     * @param <F> the type of foyer to create
     * @param scheme the scheme to register the foyer for
     * @param foyerFactory a factory function that creates the foyer
     * @return the created foyer
     * @throws NullPointerException if scheme or foyerFactory is null
     * @throws IllegalArgumentException if no runtime unit is registered for the scheme
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    @SuppressWarnings("unchecked")
    public <I extends RawInput, O extends RawOutput, F extends Foyer<I>> F createAndRegisterFoyer(
            Scheme scheme, FoyerFactory<I, O, F> foyerFactory) {
        checkInitialized();
        Objects.requireNonNull(scheme, "scheme must not be null");
        Objects.requireNonNull(foyerFactory, "foyerFactory must not be null");

        // Resolve the runtime unit for the scheme
        var unitOpt = resolveUnit(scheme);
        if (unitOpt.isEmpty()) {
            throw new IllegalArgumentException("No runtime unit registered for scheme: " + scheme);
        }

        // Get the rendezvous from the runtime unit
        var unit = (HorizonRuntimeUnit<I, ?, ?, ?, O>) unitOpt.get();
        var rendezvous = unit.getRendezvousDescriptor().rendezvous();

        // Create the foyer using the factory
        F foyer = foyerFactory.create(rendezvous);

        // Register the foyer
        registerFoyer(scheme, foyer);

        return foyer;
    }

    /**
     * A functional interface for creating foyers.
     *
     * @param <I> the type of raw input
     * @param <O> the type of raw output
     * @param <F> the type of foyer to create
     */
    @FunctionalInterface
    public interface FoyerFactory<I extends RawInput, O extends RawOutput, F extends Foyer<I>> {
        /**
         * Creates a foyer with the specified rendezvous.
         *
         * @param rendezvous the rendezvous to pass requests to
         * @return the created foyer
         */
        F create(horizon.core.rendezvous.Rendezvous<I, O> rendezvous);
    }

    /**
     * Creates and registers a protocol foyer for the specified scheme.
     * This is a convenience method that creates a protocol foyer and registers it with this context.
     * 
     * @param <I> the type of raw input
     * @param <O> the type of raw output
     * @param <F> the type of foyer to create
     * @param scheme the scheme to register the foyer for
     * @param port the port to listen on
     * @param foyerFactory a factory function that creates the foyer with the specified port and rendezvous
     * @return the created foyer
     * @throws NullPointerException if scheme or foyerFactory is null
     * @throws IllegalArgumentException if port is invalid or no runtime unit is registered for the scheme
     * @throws IllegalStateException if the context is not initialized or has been shut down
     */
    @SuppressWarnings("unchecked")
    public <I extends RawInput, O extends RawOutput, F extends ProtocolFoyer<I, O, ?, ?>> F createAndRegisterProtocolFoyer(
            Scheme scheme, int port, PortFoyerFactory<I, O, F> foyerFactory) {
        checkInitialized();
        Objects.requireNonNull(scheme, "scheme must not be null");
        Objects.requireNonNull(foyerFactory, "foyerFactory must not be null");

        // Resolve the runtime unit for the scheme
        var unitOpt = resolveUnit(scheme);
        if (unitOpt.isEmpty()) {
            throw new IllegalArgumentException("No runtime unit registered for scheme: " + scheme);
        }

        // Get the rendezvous from the runtime unit
        var unit = (HorizonRuntimeUnit<I, ?, ?, ?, O>) unitOpt.get();
        var rendezvous = unit.getRendezvousDescriptor().rendezvous();

        // Create the foyer using the factory
        F foyer = foyerFactory.create(port, rendezvous);

        // Register the foyer
        registerFoyer(scheme, foyer);

        return foyer;
    }

    /**
     * A functional interface for creating foyers with a port.
     *
     * @param <I> the type of raw input
     * @param <O> the type of raw output
     * @param <F> the type of foyer to create
     */
    @FunctionalInterface
    public interface PortFoyerFactory<I extends RawInput, O extends RawOutput, F extends Foyer<I>> {
        /**
         * Creates a foyer with the specified port and rendezvous.
         *
         * @param port the port to listen on
         * @param rendezvous the rendezvous to pass requests to
         * @return the created foyer
         */
        F create(int port, horizon.core.rendezvous.Rendezvous<I, O> rendezvous);
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
