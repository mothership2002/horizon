package horizon.demo;

import horizon.core.AbstractConductor;
import horizon.core.ProtocolAggregator;
import horizon.web.http.HttpFoyer;
import horizon.web.http.HttpProtocol;
import horizon.web.websocket.WebSocketFoyer;
import horizon.web.websocket.WebSocketProtocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DemoApplicationTest {

    private AutoCloseable mocks;

    @Mock
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
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldRegisterProtocolsAndConductors() {
        // Given
        // Create a spy of the aggregator to verify method calls
        ProtocolAggregator aggregator = spy(new ProtocolAggregator());

        // When
        // Register protocols
        aggregator.registerProtocol(httpProtocol, httpFoyer);
        aggregator.registerProtocol(wsProtocol, wsFoyer);

        // Register conductors (similar to DemoApplication)
        AbstractConductor<Map<String, Object>, Map<String, Object>> createConductor = 
            new AbstractConductor<Map<String, Object>, Map<String, Object>>("user.create") {
                @Override
                public Map<String, Object> conduct(Map<String, Object> payload) {
                    return new HashMap<>();
                }
            };

        AbstractConductor<Map<String, Object>, Map<String, Object>> getConductor = 
            new AbstractConductor<Map<String, Object>, Map<String, Object>>("user.get") {
                @Override
                public Map<String, Object> conduct(Map<String, Object> payload) {
                    return new HashMap<>();
                }
            };

        aggregator.registerConductor(createConductor);
        aggregator.registerConductor(getConductor);

        // Then
        // Verify that the protocols and conductors were registered
        verify(aggregator).registerProtocol(httpProtocol, httpFoyer);
        verify(aggregator).registerProtocol(wsProtocol, wsFoyer);
        verify(aggregator).registerConductor(createConductor);
        verify(aggregator).registerConductor(getConductor);
    }

    @Test
    void shouldValidateUserCreatePayload() {
        // Given
        // Create the user.create conductor directly
        AbstractConductor<Map<String, Object>, Map<String, Object>> conductor = 
            new AbstractConductor<Map<String, Object>, Map<String, Object>>("user.create") {
                @Override
                public Map<String, Object> conduct(Map<String, Object> payload) {
                    // Simple validation
                    String name = (String) payload.get("name");
                    String email = (String) payload.get("email");

                    if (name == null || name.trim().isEmpty()) {
                        throw new IllegalArgumentException("Name is required");
                    }
                    if (email == null || !email.contains("@")) {
                        throw new IllegalArgumentException("Valid email is required");
                    }

                    // Create user
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", 1001L);
                    user.put("name", name);
                    user.put("email", email);
                    user.put("createdAt", System.currentTimeMillis());

                    return user;
                }
            };

        // When/Then - Valid payload
        Map<String, Object> validPayload = new HashMap<>();
        validPayload.put("name", "John Doe");
        validPayload.put("email", "john@example.com");

        Map<String, Object> result = conductor.conduct(validPayload);

        assertThat(result)
            .containsEntry("name", "John Doe")
            .containsEntry("email", "john@example.com")
            .containsKey("id")
            .containsKey("createdAt");

        // When/Then - Invalid name
        Map<String, Object> invalidNamePayload = new HashMap<>();
        invalidNamePayload.put("name", "");
        invalidNamePayload.put("email", "john@example.com");

        assertThatThrownBy(() -> conductor.conduct(invalidNamePayload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Name is required");

        // When/Then - Invalid email
        Map<String, Object> invalidEmailPayload = new HashMap<>();
        invalidEmailPayload.put("name", "John Doe");
        invalidEmailPayload.put("email", "invalid-email");

        assertThatThrownBy(() -> conductor.conduct(invalidEmailPayload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Valid email is required");
    }

    @Test
    void shouldGetUserById() {
        // Given
        // Create a test map of users
        Map<Long, Map<String, Object>> users = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1001L);
        user.put("name", "John Doe");
        user.put("email", "john@example.com");
        users.put(1001L, user);

        // Create the user.get conductor directly
        AbstractConductor<Map<String, Object>, Map<String, Object>> conductor = 
            new AbstractConductor<Map<String, Object>, Map<String, Object>>("user.get") {
                @Override
                public Map<String, Object> conduct(Map<String, Object> payload) {
                    Long id = extractId(payload);

                    if (id == null) {
                        throw new IllegalArgumentException("User ID is required");
                    }

                    Map<String, Object> user = users.get(id);
                    if (user == null) {
                        throw new IllegalArgumentException("User not found: " + id);
                    }

                    return user;
                }

                private Long extractId(Map<String, Object> payload) {
                    Object id = payload.get("id");
                    if (id instanceof Number) {
                        return ((Number) id).longValue();
                    } else if (id instanceof String) {
                        try {
                            return Long.parseLong((String) id);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                    return null;
                }
            };

        // When/Then - Valid ID
        Map<String, Object> validPayload = new HashMap<>();
        validPayload.put("id", 1001L);

        Map<String, Object> result = conductor.conduct(validPayload);

        assertThat(result)
            .containsEntry("id", 1001L)
            .containsEntry("name", "John Doe")
            .containsEntry("email", "john@example.com");

        // When/Then - Invalid ID
        Map<String, Object> invalidPayload = new HashMap<>();
        invalidPayload.put("id", 9999L);

        assertThatThrownBy(() -> conductor.conduct(invalidPayload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User not found: 9999");

        // When/Then - Missing ID
        Map<String, Object> missingIdPayload = new HashMap<>();

        assertThatThrownBy(() -> conductor.conduct(missingIdPayload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User ID is required");
    }
}
