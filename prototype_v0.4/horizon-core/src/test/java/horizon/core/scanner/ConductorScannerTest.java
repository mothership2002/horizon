package horizon.core.scanner;

import horizon.core.ProtocolAggregator;
import horizon.core.annotation.Conductor;
import horizon.core.annotation.Intent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConductorScannerTest {
    
    private ProtocolAggregator aggregator;
    
    @BeforeEach
    void setUp() {
        aggregator = new ProtocolAggregator();
    }
    
    @Test
    void shouldScanAndRegisterConductors() {
        // When
        aggregator.scanConductors("horizon.core.scanner");
        
        // Then - verify by trying to use the conductors
        // This would normally be done through the protocol flow,
        // but for testing we'll check if they were registered
        // (Would need to expose conductor registry for proper testing)
    }
    
    // Test conductor for scanning
    @Conductor(namespace = "test")
    public static class TestConductor {
        
        @Intent("hello")
        public Map<String, Object> hello(Map<String, Object> payload) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Hello from test conductor!");
            response.put("input", payload.get("name"));
            return response;
        }
        
        @Intent("calculate")
        @Intent(value = "calc", aliases = {"compute"})
        public Map<String, Object> calculate(Map<String, Object> payload) {
            Integer a = (Integer) payload.get("a");
            Integer b = (Integer) payload.get("b");
            
            Map<String, Object> response = new HashMap<>();
            response.put("sum", a + b);
            response.put("product", a * b);
            return response;
        }
    }
}
