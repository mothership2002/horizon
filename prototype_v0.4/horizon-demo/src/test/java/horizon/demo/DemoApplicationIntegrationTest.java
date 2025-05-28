package horizon.demo;

import horizon.core.ProtocolAggregator;
import horizon.web.http.HttpFoyer;
import horizon.web.http.HttpProtocol;
import horizon.web.websocket.WebSocketFoyer;
import horizon.web.websocket.WebSocketProtocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the DemoApplication.
 * This test class focuses on testing the application initialization and component integration.
 */
class DemoApplicationIntegrationTest {

    private ProtocolAggregator aggregator;
    private HttpProtocol httpProtocol;
    private HttpFoyer httpFoyer;
    private WebSocketProtocol wsProtocol;
    private WebSocketFoyer wsFoyer;

    @BeforeEach
    void setUp() {
        // Initialize components manually, similar to how DemoApplication does it
        aggregator = new ProtocolAggregator();
        httpProtocol = new HttpProtocol();
        httpFoyer = new HttpFoyer(8080);
        wsProtocol = new WebSocketProtocol();
        wsFoyer = new WebSocketFoyer(8081);

        // Register protocols
        aggregator.registerProtocol(httpProtocol, httpFoyer);
        aggregator.registerProtocol(wsProtocol, wsFoyer);

        // Scan and register conductors
        aggregator.scanConductors("horizon.demo.conductors");
    }

    @Test
    @DisplayName("Should initialize components properly")
    void shouldInitializeComponentsProperly() {
        // Verify that components are initialized
        assertThat(aggregator).isNotNull();
        assertThat(httpProtocol).isNotNull();
        assertThat(httpFoyer).isNotNull();
        assertThat(wsProtocol).isNotNull();
        assertThat(wsFoyer).isNotNull();
    }

    @Test
    @DisplayName("Should register protocols and conductors")
    void shouldRegisterProtocolsAndConductors() {
        // Check for specific conductor methods
        assertThat(aggregator.getConductorMethod("user.create")).isNotNull();
        assertThat(aggregator.getConductorMethod("user.get")).isNotNull();
        assertThat(aggregator.getConductorMethod("user.update")).isNotNull();
        assertThat(aggregator.getConductorMethod("user.delete")).isNotNull();
    }
}
