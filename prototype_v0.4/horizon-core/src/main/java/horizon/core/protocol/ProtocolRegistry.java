package horizon.core.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all protocols in the system.
 * This provides a clean separation between protocol registration and aggregation.
 */
public class ProtocolRegistry {
    private static final ProtocolRegistry INSTANCE = new ProtocolRegistry();
    
    private final Map<String, Protocol<?, ?>> protocols = new ConcurrentHashMap<>();
    private final Map<String, ProtocolMetadata> metadata = new ConcurrentHashMap<>();
    
    private ProtocolRegistry() {}
    
    public static ProtocolRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Registers a protocol with metadata.
     */
    public <I, O> void register(Protocol<I, O> protocol, ProtocolMetadata metadata) {
        String name = protocol.getName();
        this.protocols.put(name, protocol);
        this.metadata.put(name, metadata);
    }
    
    /**
     * Gets a registered protocol.
     */
    @SuppressWarnings("unchecked")
    public <I, O> Protocol<I, O> getProtocol(String name) {
        return (Protocol<I, O>) protocols.get(name);
    }
    
    /**
     * Gets protocol metadata.
     */
    public ProtocolMetadata getMetadata(String name) {
        return metadata.get(name);
    }
    
    /**
     * Checks if a protocol is registered.
     */
    public boolean isRegistered(String name) {
        return protocols.containsKey(name);
    }
    
    /**
     * Gets all registered protocol names.
     */
    public Map<String, Protocol<?, ?>> getAllProtocols() {
        return new ConcurrentHashMap<>(protocols);
    }
    
    /**
     * Protocol metadata for configuration and capabilities.
     */
    public static class ProtocolMetadata {
        private final boolean supportsStreaming;
        private final boolean supportsBidirectional;
        private final int defaultPort;
        private final String version;
        
        public ProtocolMetadata(boolean supportsStreaming, boolean supportsBidirectional, 
                               int defaultPort, String version) {
            this.supportsStreaming = supportsStreaming;
            this.supportsBidirectional = supportsBidirectional;
            this.defaultPort = defaultPort;
            this.version = version;
        }
        
        // Getters
        public boolean supportsStreaming() { return supportsStreaming; }
        public boolean supportsBidirectional() { return supportsBidirectional; }
        public int getDefaultPort() { return defaultPort; }
        public String getVersion() { return version; }
    }
}
