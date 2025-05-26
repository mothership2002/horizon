package horizon.core;

import horizon.core.conductor.ConductorMethod;
import horizon.core.protocol.Protocol;
import horizon.core.protocol.ProtocolAdapter;
import horizon.core.scanner.ConductorScanner;
import horizon.core.security.ProtocolAccessValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The heart of Horizon Framework - aggregates multiple protocols into a unified processing pipeline.
 * This is where the magic happens: different protocols converge at a single Rendezvous point.
 */
public class ProtocolAggregator {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolAggregator.class);

    private final Map<String, Protocol<?, ?>> protocols = new ConcurrentHashMap<>();
    private final Map<String, Foyer<?>> foyers = new ConcurrentHashMap<>();
    private final Map<String, ProtocolAdapter<?, ?>> adapters = new ConcurrentHashMap<>();
    private final ConductorRegistry conductorRegistry = new ConductorRegistry();
    private final Map<String, ConductorMethod> conductorMethods = new ConcurrentHashMap<>();
    private final CentralRendezvous centralRendezvous;

    public ProtocolAggregator() {
        this.centralRendezvous = new CentralRendezvous(conductorRegistry, conductorMethods);
    }

    /**
     * Registers a protocol with this aggregator.
     * This will create a Foyer for the protocol and connect it to the central Rendezvous.
     *
     * @param protocol the protocol to register
     * @param foyer the foyer for this protocol
     */
    public <I, O> void registerProtocol(Protocol<I, O> protocol, Foyer<I> foyer) {
        String protocolName = protocol.getName();
        logger.info("Registering protocol: {}", protocolName);

        protocols.put(protocolName, protocol);
        foyers.put(protocolName, foyer);
        
        // Create and store the adapter
        ProtocolAdapter<I, O> adapter = protocol.createAdapter();
        adapters.put(protocolName, adapter);

        // Create a protocol-specific rendezvous that delegates to the central one
        ProtocolSpecificRendezvous<I, O> protocolRendezvous = 
            new ProtocolSpecificRendezvous<>(protocol, adapter, centralRendezvous);

        foyer.connectToRendezvous(protocolRendezvous);
    }

    /**
     * Gets the protocol adapter for a specific protocol.
     * This is used by the ConductorScanner to register protocol-specific mappings.
     */
    @SuppressWarnings("unchecked")
    public <I, O> ProtocolAdapter<I, O> getProtocolAdapter(String protocolName) {
        return (ProtocolAdapter<I, O>) adapters.get(protocolName);
    }

    /**
     * Registers a conductor for handling specific intents.
     *
     * @param conductor the conductor to register
     */
    public void registerConductor(Conductor<?, ?> conductor) {
        logger.info("Registering conductor for pattern: {}", conductor.getIntentPattern());
        conductorRegistry.register(conductor);
    }
    
    /**
     * Registers a conductor method.
     * This is used by the ConductorScanner to register method-level information.
     */
    public void registerConductorMethod(ConductorMethod method) {
        conductorMethods.put(method.getIntent(), method);
        registerConductor(new ConductorScanner.ConductorMethodAdapter(method));
    }

    /**
     * Scans the specified package for classes annotated with @Conductor and registers them.
     * This is a convenient way to register all conductors in a package without manually
     * instantiating and registering each one.
     *
     * @param basePackage the base package to scan
     */
    public void scanConductors(String basePackage) {
        logger.info("Scanning for conductors in package: {}", basePackage);
        ConductorScanner scanner = new ConductorScanner();
        List<ConductorMethod> methods = scanner.scan(basePackage, this);
        
        // Register all conductor methods
        for (ConductorMethod method : methods) {
            registerConductorMethod(method);
        }
    }

    /**
     * Starts all registered foyers, beginning to accept requests.
     */
    public void start() {
        logger.info("Starting Protocol Aggregator with {} protocols", protocols.size());

        for (Map.Entry<String, Foyer<?>> entry : foyers.entrySet()) {
            logger.info("Opening foyer for protocol: {}", entry.getKey());
            entry.getValue().open();
        }

        logger.info("Protocol Aggregator started successfully");
    }

    /**
     * Stops all registered foyers.
     */
    public void stop() {
        logger.info("Stopping Protocol Aggregator");

        for (Map.Entry<String, Foyer<?>> entry : foyers.entrySet()) {
            logger.info("Closing foyer for protocol: {}", entry.getKey());
            try {
                entry.getValue().close();
            } catch (Exception e) {
                logger.error("Error closing foyer for protocol: {}", entry.getKey(), e);
            }
        }

        logger.info("Protocol Aggregator stopped");
    }

    /**
     * Gets the ConductorMethod for a specific intent.
     * This is used by protocol adapters to determine parameter types.
     */
    public ConductorMethod getConductorMethod(String intent) {
        return conductorMethods.get(intent);
    }

    /**
     * Inner class that handles the central meeting point for all protocols.
     */
    private static class CentralRendezvous {
        private final ConductorRegistry conductorRegistry;
        private final Map<String, ConductorMethod> conductorMethods;
        private final ProtocolAccessValidator accessValidator;

        CentralRendezvous(ConductorRegistry conductorRegistry, Map<String, ConductorMethod> conductorMethods) {
            this.conductorRegistry = conductorRegistry;
            this.conductorMethods = conductorMethods;
            this.accessValidator = new ProtocolAccessValidator();
        }

        @SuppressWarnings("unchecked")
        HorizonContext process(HorizonContext context) {
            String intent = context.getIntent();
            String protocol = (String) context.getAttribute("protocol");
            logger.debug("Processing intent: {} from protocol: {} [{}]", intent, protocol, context.getTraceId());

            try {
                // Find conductor for this intent
                Conductor<Object, Object> conductor = conductorRegistry.find(intent);
                if (conductor == null) {
                    throw new IllegalArgumentException("No conductor found for intent: " + intent);
                }
                
                // Validate protocol access
                if (conductor instanceof ConductorScanner.ConductorMethodAdapter) {
                    ConductorScanner.ConductorMethodAdapter adapter = 
                        (ConductorScanner.ConductorMethodAdapter) conductor;
                    if (!accessValidator.hasAccess(protocol, adapter.method)) {
                        throw new SecurityException(
                            String.format("Protocol '%s' is not allowed to access intent '%s'", protocol, intent)
                        );
                    }
                }

                // Conduct the business logic
                Object result = conductor.conduct(context.getPayload());
                context.setResult(result);

                logger.debug("Successfully processed intent: {} [{}]", intent, context.getTraceId());
            } catch (Exception e) {
                logger.error("Error processing intent: {} [{}]", intent, context.getTraceId(), e);
                context.setError(e);
            }

            return context;
        }
    }

    /**
     * Adapts protocol-specific requests to the central rendezvous.
     */
    public static class ProtocolSpecificRendezvous<I, O> implements Rendezvous<I, O> {
        private static final Logger logger = LoggerFactory.getLogger(ProtocolSpecificRendezvous.class);
        
        private final Protocol<I, O> protocol;
        private final ProtocolAdapter<I, O> adapter;
        private final CentralRendezvous centralRendezvous;

        ProtocolSpecificRendezvous(Protocol<I, O> protocol, ProtocolAdapter<I, O> adapter, CentralRendezvous centralRendezvous) {
            this.protocol = protocol;
            this.adapter = adapter;
            this.centralRendezvous = centralRendezvous;
        }

        @Override
        public HorizonContext encounter(I input) {
            logger.debug("Encountering [{}] request", protocol.getName());

            // Extract intent and payload using protocol adapter
            String intent = adapter.extractIntent(input);
            Object payload = adapter.extractPayload(input);

            // Create context
            HorizonContext context = new HorizonContext();
            context.setIntent(intent);
            context.setPayload(payload);
            context.setAttribute("protocol", protocol.getName());
            context.setAttribute("originalRequest", input);

            // Process through central rendezvous
            return centralRendezvous.process(context);
        }

        @Override
        @SuppressWarnings("unchecked")
        public O fallAway(HorizonContext context) {
            logger.debug("Falling away with {} response [{}]", protocol.getName(), context.getTraceId());

            I originalRequest = (I) context.getAttribute("originalRequest");

            if (context.hasError()) {
                return adapter.buildErrorResponse(context.getError(), originalRequest);
            } else {
                return adapter.buildResponse(context.getResult(), originalRequest);
            }
        }
    }
}
