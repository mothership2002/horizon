package horizon.core;

import horizon.core.protocol.Protocol;
import horizon.core.protocol.ProtocolAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProtocolAggregatorTest {
    
    private ProtocolAggregator aggregator;
    
    @BeforeEach
    void setUp() {
        aggregator = new ProtocolAggregator();
    }
    
    @Test
    void shouldProcessRequestThroughConductor() {
        // Given
        TestProtocol protocol = new TestProtocol();
        TestFoyer foyer = new TestFoyer();
        aggregator.registerProtocol(protocol, foyer);
        
        // Register a conductor
        aggregator.registerConductor(new AbstractConductor<Map<String, Object>, String>("test.intent") {
            @Override
            public String conduct(Map<String, Object> payload) {
                return "Hello, " + payload.get("name");
            }
        });
        
        // When
        aggregator.start();
        
        // Simulate request processing
        Map<String, Object> request = new HashMap<>();
        request.put("intent", "test.intent");
        request.put("name", "World");
        
        HorizonContext context = foyer.simulateRequest(request);
        
        // Then
        assertThat(context.getResult()).isEqualTo("Hello, World");
        assertThat(context.hasError()).isFalse();
        
        aggregator.stop();
    }
    
    // Test implementations
    private static class TestProtocol implements Protocol<Map<String, Object>, Map<String, Object>> {
        @Override
        public String getName() {
            return "TEST";
        }
        
        @Override
        public ProtocolAdapter<Map<String, Object>, Map<String, Object>> createAdapter() {
            return new TestAdapter();
        }
    }
    
    private static class TestAdapter implements ProtocolAdapter<Map<String, Object>, Map<String, Object>> {
        @Override
        public String extractIntent(Map<String, Object> request) {
            return (String) request.get("intent");
        }
        
        @Override
        public Object extractPayload(Map<String, Object> request) {
            Map<String, Object> payload = new HashMap<>(request);
            payload.remove("intent");
            return payload;
        }
        
        @Override
        public Map<String, Object> buildResponse(Object result, Map<String, Object> request) {
            Map<String, Object> response = new HashMap<>();
            response.put("result", result);
            return response;
        }
        
        @Override
        public Map<String, Object> buildErrorResponse(Throwable error, Map<String, Object> request) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", error.getMessage());
            return response;
        }
    }
    
    private static class TestFoyer implements Foyer<Map<String, Object>> {
        private Rendezvous<Map<String, Object>, Map<String, Object>> rendezvous;
        private boolean open = false;
        
        @Override
        public void open() {
            open = true;
        }
        
        @Override
        public void close() {
            open = false;
        }
        
        @Override
        public boolean isOpen() {
            return open;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public void connectToRendezvous(Rendezvous<Map<String, Object>, ?> rendezvous) {
            this.rendezvous = (Rendezvous<Map<String, Object>, Map<String, Object>>) rendezvous;
        }
        
        // Helper method for testing
        public HorizonContext simulateRequest(Map<String, Object> request) {
            if (rendezvous != null) {
                HorizonContext context = rendezvous.encounter(request);
                rendezvous.fallAway(context);
                return context;
            }
            return null;
        }
    }
}
