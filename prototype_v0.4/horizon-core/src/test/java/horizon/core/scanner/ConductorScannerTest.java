package horizon.core.scanner;

import horizon.core.Conductor;
import horizon.core.ProtocolAggregator;
import horizon.core.annotation.Intent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConductorScannerTest {

    private ProtocolAggregator aggregator;

    @BeforeEach
    void setUp() {
        // Use a spy instead of a real aggregator to verify method calls
        aggregator = spy(new ProtocolAggregator());
    }

    @Test
    void shouldScanAndRegisterConductors() {
        // When
        aggregator.scanConductors("horizon.core.scanner");

        // Then - verify that registerConductor was called for each intent method
        // Capture all conductors that were registered
        ArgumentCaptor<Conductor> conductorCaptor = ArgumentCaptor.forClass(Conductor.class);
        verify(aggregator, atLeast(3)).registerConductor(conductorCaptor.capture());

        // Get all registered intents
        Set<String> registeredIntents = new HashSet<>();
        for (Conductor conductor : conductorCaptor.getAllValues()) {
            registeredIntents.add(conductor.getIntentPattern());
        }

        // Verify that all expected intents were registered
        assertThat(registeredIntents).contains(
            "test.hello",    // From @Intent("hello")
            "test.calc",     // From @Intent(value = "calc", ...)
            "test.compute"   // From aliases = {"compute"}
        );

        // Verify that we can use the registered conductors
        // Create test payloads
        Map<String, Object> helloPayload = new HashMap<>();
        helloPayload.put("name", "World");

        Map<String, Object> calcPayload = new HashMap<>();
        calcPayload.put("a", 5);
        calcPayload.put("b", 7);

        // Find the conductors for each intent
        Conductor helloConductor = findConductor(conductorCaptor.getAllValues(), "test.hello");
        Conductor calcConductor = findConductor(conductorCaptor.getAllValues(), "test.calc");
        Conductor computeConductor = findConductor(conductorCaptor.getAllValues(), "test.compute");

        // Verify that the conductors were found
        assertThat(helloConductor).isNotNull();
        assertThat(calcConductor).isNotNull();
        assertThat(computeConductor).isNotNull();

        // Verify that the conductors work correctly
        Map<String, Object> helloResult = (Map<String, Object>) helloConductor.conduct(helloPayload);
        assertThat(helloResult).containsEntry("message", "Hello from test conductor!");
        assertThat(helloResult).containsEntry("input", "World");

        Map<String, Object> calcResult = (Map<String, Object>) calcConductor.conduct(calcPayload);
        assertThat(calcResult).containsEntry("sum", 12);
        assertThat(calcResult).containsEntry("product", 35);

        // Verify that the compute alias points to the same method as calc
        Map<String, Object> computeResult = (Map<String, Object>) computeConductor.conduct(calcPayload);
        assertThat(computeResult).isEqualTo(calcResult);
    }

    // Helper method to find a conductor by intent pattern
    private Conductor findConductor(Iterable<Conductor> conductors, String intentPattern) {
        for (Conductor conductor : conductors) {
            if (conductor.getIntentPattern().equals(intentPattern)) {
                return conductor;
            }
        }
        return null;
    }

    // Test conductor for scanning
    @horizon.core.annotation.Conductor(namespace = "test")
    public static class TestConductor {

        @Intent("hello")
        public Map<String, Object> hello(Map<String, Object> payload) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Hello from test conductor!");
            response.put("input", payload.get("name"));
            return response;
        }

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
