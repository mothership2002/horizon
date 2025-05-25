package horizon.core;

import horizon.core.annotation.ProtocolAccess;
import horizon.core.protocol.Protocol;
import horizon.core.protocol.ProtocolAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProtocolAggregatorTest {

    private ProtocolAggregator aggregator;
    private AutoCloseable mocks;

    @Mock
    private Protocol<Object, Object> mockProtocol;

    @Mock
    private Foyer<Object> mockFoyer;

    @Mock
    private ProtocolAdapter<Object, Object> mockAdapter;

    @Mock
    private Rendezvous<Object, Object> mockRendezvous;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        aggregator = new ProtocolAggregator();

        // Setup mock protocol
        when(mockProtocol.getName()).thenReturn("TEST");
        when(mockProtocol.createAdapter()).thenReturn(mockAdapter);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testConductorRegistration() {
        // Given
        TestConductor conductor = new TestConductor("test.intent");

        // When
        aggregator.registerConductor(conductor);

        // Then
        // We need to verify that the conductor was registered
        // We can do this by registering a protocol and starting the aggregator
        aggregator.registerProtocol(mockProtocol, mockFoyer);

        // Verify that the protocol and foyer were registered
        verify(mockFoyer).connectToRendezvous(any(Rendezvous.class));

        // Start and stop the aggregator to verify it works
        aggregator.start();
        verify(mockFoyer).open();

        aggregator.stop();
        verify(mockFoyer).close();
    }

    @Test
    void testProtocolAccessControl() {
        // Given
        // Create a test conductor with protocol access restrictions
        TestConductorWithAccess conductor = new TestConductorWithAccess("restricted.intent");
        aggregator.registerConductor(conductor);

        // Create a mock protocol adapter that will extract our test intent
        when(mockAdapter.extractIntent(any())).thenReturn("restricted.intent");
        Map<String, Object> testPayload = new HashMap<>();
        when(mockAdapter.extractPayload(any())).thenReturn(testPayload);

        // Create a mock foyer that will capture the rendezvous
        doAnswer(invocation -> {
            mockRendezvous = invocation.getArgument(0);
            return null;
        }).when(mockFoyer).connectToRendezvous(any(Rendezvous.class));

        // Register the protocol
        when(mockProtocol.getName()).thenReturn("ALLOWED");
        aggregator.registerProtocol(mockProtocol, mockFoyer);

        // When - Test with allowed protocol
        HorizonContext context = mockRendezvous.encounter(new Object());

        // Then
        assertNotNull(context);
        assertFalse(context.hasError(), "Should not have error with allowed protocol");
        assertEquals("Restricted access: {}", context.getResult());

        // When - Test with denied protocol
        when(mockProtocol.getName()).thenReturn("DENIED");
        context = mockRendezvous.encounter(new Object());

        // Then
        assertNotNull(context);
        assertTrue(context.hasError(), "Should have error with denied protocol");
        assertTrue(context.getError() instanceof SecurityException, 
            "Error should be SecurityException but was: " + 
            (context.hasError() ? context.getError().getClass().getName() : "no error"));
    }

    // Test conductor for registration test
    private static class TestConductor implements Conductor<Map<String, Object>, String> {
        private final String pattern;

        TestConductor(String pattern) {
            this.pattern = pattern;
        }

        @Override
        public String conduct(Map<String, Object> payload) {
            return "Conducted: " + payload;
        }

        @Override
        public String getIntentPattern() {
            return pattern;
        }
    }

    // Test conductor with protocol access restrictions
    private static class TestConductorWithAccess implements Conductor<Map<String, Object>, String> {
        private final String pattern;

        TestConductorWithAccess(String pattern) {
            this.pattern = pattern;
        }

        @ProtocolAccess(value = {"ALLOWED"})
        @Override
        public String conduct(Map<String, Object> payload) {
            return "Restricted access: " + payload;
        }

        @Override
        public String getIntentPattern() {
            return pattern;
        }
    }
}
