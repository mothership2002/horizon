package horizon.core;

import horizon.core.conductor.ConductorMethod;
import horizon.core.conductor.ConductorMethodCache;
import horizon.core.protocol.Protocol;
import horizon.core.protocol.ProtocolAdapter;
import horizon.core.scanner.ConductorScanner;
import horizon.core.security.ProtocolAccessValidator;
import horizon.core.metrics.MetricsCollector;
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
    private final CentralRendezvous centralRendezvous;
    private final ProtocolAccessValidator accessValidator = new ProtocolAccessValidator();

    public ProtocolAggregator() {
        this.centralRendezvous = new CentralRendezvous();
    }

    /**
     * Registers a protocol with this aggregator.
     */
    public <I, O> void registerProtocol(Protocol<I, O> protocol, Foyer<I> foyer) {
        String protocolName = protocol.getName();
        logger.info("Registering protocol: {}", protocolName);

        protocols.put(protocolName, protocol);
        foyers.put(protocolName, foyer);
        
        // Create and store the adapter
        ProtocolAdapter<I, O> adapter = protocol.createAdapter();
        adapters.put(protocolName, adapter);
        
        // Set aggregator reference if the adapter implements AggregatorAware
        if (adapter instanceof horizon.core.protocol.AggregatorAware) {
            ((horizon.core.protocol.AggregatorAware) adapter).setProtocolAggregator(this);
        }

        // Create a protocol-specific rendezvous that delegates to the central one
        ProtocolSpecificRendezvous<I, O> protocolRendezvous = 
            new ProtocolSpecificRendezvous<>(protocol, adapter, centralRendezvous);

        foyer.connectToRendezvous(protocolRendezvous);
    }

    /**
     * Gets the protocol adapter for a specific protocol.
     */
    @SuppressWarnings("unchecked")
    public <I, O> ProtocolAdapter<I, O> getProtocolAdapter(String protocolName) {
        return (ProtocolAdapter<I, O>) adapters.get(protocolName);
    }

    /**
     * Registers a conductor method.
     */
    public void registerConductorMethod(ConductorMethod method) {
        conductorRegistry.register(method);
        ConductorMethodCache.getInstance().cache(method.getIntent(), method);
    }

    /**
     * Scans the specified package for classes annotated with @Conductor.
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
     * Starts all registered foyers.
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
     * Gets the ConductorMethod for specific intent.
     */
    public ConductorMethod getConductorMethod(String intent) {
        return conductorRegistry.find(intent);
    }

    /**
     * Central meeting point for all protocols.
     */
    private class CentralRendezvous {
        
        HorizonContext process(HorizonContext context) {
            String intent = context.getIntent();
            String protocol = (String) context.getAttribute("protocol");
            logger.debug("Processing intent: {} from protocol: {} [{}]", intent, protocol, context.getTraceId());

            // Metrics
            MetricsCollector metrics = MetricsCollector.getInstance();
            metrics.incrementCounter("requests.total");
            metrics.incrementCounter("requests.protocol." + protocol);
            metrics.incrementCounter("requests.intent." + intent);
            
            long startTime = System.currentTimeMillis();

            try {
                // Find conductor method for this intent
                ConductorMethod method = conductorRegistry.find(intent);
                if (method == null) {
                    metrics.incrementCounter("errors.intent_not_found");
                    throw new IllegalArgumentException("No conductor found for intent: " + intent);
                }
                
                // Validate protocol access
                if (!accessValidator.hasAccess(protocol, method)) {
                    metrics.incrementCounter("errors.access_denied");
                    throw new SecurityException(
                        String.format("Protocol '%s' is not allowed to access intent '%s'", protocol, intent)
                    );
                }

                // Invoke conductor method
                Object result = method.invoke(context.getPayload());
                context.setResult(result);

                metrics.incrementCounter("requests.success");
                logger.debug("Successfully processed intent: {} [{}]", intent, context.getTraceId());
                
            } catch (Exception e) {
                metrics.incrementCounter("requests.error");
                metrics.incrementCounter("errors." + e.getClass().getSimpleName());
                logger.error("Error processing intent: {} [{}]", intent, context.getTraceId(), e);
                context.setError(e);
            } finally {
                // Record timing
                long duration = System.currentTimeMillis() - startTime;
                metrics.recordTiming("request.duration", duration);
                metrics.recordTiming("request.duration." + intent, duration);
            }

            return context;
        }
    }

    /**
     * Protocol-specific rendezvous implementation.
     */
    private static class ProtocolSpecificRendezvous<I, O> implements Rendezvous<I, O> {
        private static final Logger logger = LoggerFactory.getLogger(ProtocolSpecificRendezvous.class);
        
        private final Protocol<I, O> protocol;
        private final ProtocolAdapter<I, O> adapter;
        private final CentralRendezvous centralRendezvous;

        ProtocolSpecificRendezvous(Protocol<I, O> protocol, ProtocolAdapter<I, O> adapter, 
                                  CentralRendezvous centralRendezvous) {
            this.protocol = protocol;
            this.adapter = adapter;
            this.centralRendezvous = centralRendezvous;
        }

        @Override
        public HorizonContext encounter(I input) {
            logger.debug("Encountering {} request", protocol.getName());

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
