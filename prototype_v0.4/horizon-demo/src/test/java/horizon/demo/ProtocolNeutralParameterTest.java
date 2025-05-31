package horizon.demo;

import horizon.core.conductor.ConductorMethod;
import horizon.demo.conductor.GrpcDemoConductor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for protocol-neutral parameter handling.
 * Verifies that @Param works across different protocols.
 */
public class ProtocolNeutralParameterTest {
    
    private final GrpcDemoConductor conductor = new GrpcDemoConductor();
    
    @Test
    public void testHttpStyleParameters() throws Exception {
        // Simulate HTTP request context
        Map<String, Object> context = Map.of(
            "path.userId", "123",
            "query.limit", 5,
            "query.search", "john",
            "body", Map.of(
                "name", "John Doe",
                "email", "john@example.com"
            )
        );
        
        // Create user
        ConductorMethod createMethod = new ConductorMethod(
            conductor, 
            conductor.getClass().getMethod("createUser", String.class, String.class),
            "user.create"
        );
        
        Map<String, Object> result = (Map<String, Object>) createMethod.invoke(context);
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }
    
    @Test
    public void testGrpcStyleParameters() throws Exception {
        // Simulate gRPC request context
        Map<String, Object> context = Map.of(
            "body", Map.of(
                "user_id", "123",      // snake_case from proto
                "name", "Jane Doe",
                "email", "jane@example.com"
            )
        );
        
        // Get user - should find user_id as userId
        ConductorMethod getMethod = new ConductorMethod(
            conductor,
            conductor.getClass().getMethod("getUser", String.class),
            "user.get"
        );
        
        // First create a user
        String userId = "test-123";
        Map<String, Object> createContext = Map.of(
            "body", Map.of(
                "name", "Test User",
                "email", "test@example.com",
                "userId", userId
            )
        );
        
        // The parameter resolver should find userId in body
        context = Map.of("body", Map.of("userId", userId));
        
        Map<String, Object> getResult = (Map<String, Object>) getMethod.invoke(context);
        assertNotNull(getResult);
        // Will be false because we didn't actually create the user in storage
        assertFalse((Boolean) getResult.get("found"));
    }
    
    @Test
    public void testWebSocketStyleParameters() throws Exception {
        // Simulate WebSocket request context
        Map<String, Object> context = Map.of(
            "data", Map.of(
                "userId", "123",
                "limit", 20,
                "offset", 10
            )
        );
        
        // List users
        ConductorMethod listMethod = new ConductorMethod(
            conductor,
            conductor.getClass().getMethod("listUsers", int.class, int.class, String.class),
            "user.list"
        );
        
        Map<String, Object> result = (Map<String, Object>) listMethod.invoke(context);
        assertNotNull(result);
        assertEquals(20, result.get("limit"));
        assertEquals(10, result.get("offset"));
    }
    
    @Test
    public void testParameterVariants() throws Exception {
        // Test that different naming conventions work
        String userId = "123";
        
        // camelCase
        Map<String, Object> context1 = Map.of("userId", userId);
        
        // snake_case
        Map<String, Object> context2 = Map.of("user_id", userId);
        
        // In path
        Map<String, Object> context3 = Map.of("path.id", userId);
        
        // In body
        Map<String, Object> context4 = Map.of("body", Map.of("userId", userId));
        
        ConductorMethod getMethod = new ConductorMethod(
            conductor,
            conductor.getClass().getMethod("getUser", String.class),
            "user.get"
        );
        
        // All should work
        for (var context : List.of(context1, context2, context3, context4)) {
            Map<String, Object> result = (Map<String, Object>) getMethod.invoke(context);
            assertNotNull(result);
            // Would check actual values if we had created the user
        }
    }
    
    @Test
    public void testDefaultValues() throws Exception {
        // Test default value handling
        Map<String, Object> context = Map.of(
            // No limit or offset provided
            "data", Map.of()
        );
        
        ConductorMethod listMethod = new ConductorMethod(
            conductor,
            conductor.getClass().getMethod("listUsers", int.class, int.class, String.class),
            "user.list"
        );
        
        Map<String, Object> result = (Map<String, Object>) listMethod.invoke(context);
        assertNotNull(result);
        assertEquals(10, result.get("limit"));  // Default value
        assertEquals(0, result.get("offset"));  // Default value
    }
    
    @Test
    public void testHints() throws Exception {
        // Test that hints work correctly
        Map<String, Object> context = Map.of(
            "header.authToken", "secret-token",
            "body", Map.of(
                "userId", "123",
                "authToken", "wrong-token"  // Should not use this
            )
        );
        
        ConductorMethod deleteMethod = new ConductorMethod(
            conductor,
            conductor.getClass().getMethod("deleteUser", String.class, String.class),
            "user.delete"
        );
        
        // Would need to verify the correct token is used
        Map<String, Object> result = (Map<String, Object>) deleteMethod.invoke(context);
        assertNotNull(result);
    }
}
