package horizon.demo.core;

import horizon.core.Conductor;
import horizon.core.ProtocolAggregator;
import horizon.web.http.HttpFoyer;
import horizon.web.http.HttpProtocol;
import horizon.web.websocket.WebSocketFoyer;
import horizon.web.websocket.WebSocketProtocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Tests for the ProtocolAggregator class.
 * This test class focuses on testing the registration of protocols and conductors.
 */
class ProtocolAggregatorTest {

    private AutoCloseable mocks;
    private ProtocolAggregator aggregator;

    @Mock
    private HttpProtocol httpProtocol;

    @Mock
    private HttpFoyer httpFoyer;

    @Mock
    private WebSocketProtocol wsProtocol;

    @Mock
    private WebSocketFoyer wsFoyer;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        aggregator = spy(new ProtocolAggregator());
    }

    @Test
    @DisplayName("Should register protocols")
    void shouldRegisterProtocols() {
        // When
        aggregator.registerProtocol(httpProtocol, httpFoyer);
        aggregator.registerProtocol(wsProtocol, wsFoyer);

        // Then
        verify(aggregator).registerProtocol(httpProtocol, httpFoyer);
        verify(aggregator).registerProtocol(wsProtocol, wsFoyer);
    }

    @Test
    @DisplayName("Should register conductors")
    void shouldRegisterConductors() {
        // Given
        Conductor<Map<String, Object>, Map<String, Object>> createConductor = 
            new Conductor<Map<String, Object>, Map<String, Object>>() {
                @Override
                public Map<String, Object> conduct(Map<String, Object> payload) {
                    return new HashMap<>();
                }

                @Override
                public String getIntentPattern() {
                    return "user.create";
                }
            };

        Conductor<Map<String, Object>, Map<String, Object>> getConductor = 
            new Conductor<Map<String, Object>, Map<String, Object>>() {
                @Override
                public Map<String, Object> conduct(Map<String, Object> payload) {
                    return new HashMap<>();
                }

                @Override
                public String getIntentPattern() {
                    return "user.get";
                }
            };

        // When
        aggregator.registerConductor(createConductor);
        aggregator.registerConductor(getConductor);

        // Then
        verify(aggregator).registerConductor(createConductor);
        verify(aggregator).registerConductor(getConductor);
    }

    @Test
    @DisplayName("Should register both protocols and conductors")
    void shouldRegisterProtocolsAndConductors() {
        // Given
        Conductor<Map<String, Object>, Map<String, Object>> createConductor = 
            new Conductor<Map<String, Object>, Map<String, Object>>() {
                @Override
                public Map<String, Object> conduct(Map<String, Object> payload) {
                    return new HashMap<>();
                }

                @Override
                public String getIntentPattern() {
                    return "user.create";
                }
            };

        Conductor<Map<String, Object>, Map<String, Object>> getConductor = 
            new Conductor<Map<String, Object>, Map<String, Object>>() {
                @Override
                public Map<String, Object> conduct(Map<String, Object> payload) {
                    return new HashMap<>();
                }

                @Override
                public String getIntentPattern() {
                    return "user.get";
                }
            };

        // When
        aggregator.registerProtocol(httpProtocol, httpFoyer);
        aggregator.registerProtocol(wsProtocol, wsFoyer);
        aggregator.registerConductor(createConductor);
        aggregator.registerConductor(getConductor);

        // Then
        verify(aggregator).registerProtocol(httpProtocol, httpFoyer);
        verify(aggregator).registerProtocol(wsProtocol, wsFoyer);
        verify(aggregator).registerConductor(createConductor);
        verify(aggregator).registerConductor(getConductor);
    }
}
