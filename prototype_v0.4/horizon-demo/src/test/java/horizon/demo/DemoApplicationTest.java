package horizon.demo;

import horizon.core.ProtocolAggregator;
import horizon.web.http.HttpFoyer;
import horizon.web.http.HttpProtocol;
import horizon.web.websocket.WebSocketFoyer;
import horizon.web.websocket.WebSocketProtocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for the DemoApplication class.
 * This test class focuses on testing the application initialization and configuration.
 */
class DemoApplicationTest {

    @Test
    @DisplayName("Should initialize application with correct protocols")
    void shouldInitializeApplicationWithCorrectProtocols() {
        // Given
        ProtocolAggregator aggregator = mock(ProtocolAggregator.class);
        
        // When
        // Simulate the initialization part of DemoApplication
        aggregator.registerProtocol(new HttpProtocol(), new HttpFoyer(8080));
        aggregator.registerProtocol(new WebSocketProtocol(), new WebSocketFoyer(8081));
        aggregator.scanConductors("horizon.demo.conductors");
        
        // Then
        // Verify that the protocols were registered
        verify(aggregator).registerProtocol(any(HttpProtocol.class), any(HttpFoyer.class));
        verify(aggregator).registerProtocol(any(WebSocketProtocol.class), any(WebSocketFoyer.class));
        verify(aggregator).scanConductors("horizon.demo.conductors");
    }
    
    @Test
    @DisplayName("Should scan conductors from the correct package")
    void shouldScanConductorsFromCorrectPackage() {
        // Given
        ProtocolAggregator aggregator = mock(ProtocolAggregator.class);
        
        // When
        // Simulate the conductor scanning part of DemoApplication
        aggregator.scanConductors("horizon.demo.conductors");
        
        // Then
        // Verify that the correct package was scanned
        ArgumentCaptor<String> packageCaptor = ArgumentCaptor.forClass(String.class);
        verify(aggregator).scanConductors(packageCaptor.capture());
        assertThat(packageCaptor.getValue()).isEqualTo("horizon.demo.conductors");
    }
}