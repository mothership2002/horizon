package horizon.demo.conductor;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Legacy conductor using HTTP-specific annotations.
 * This shows the old way of handling parameters - still supported but deprecated.
 * 
 * @deprecated Use @Param annotation for protocol-neutral parameter handling.
 *            See {@link GrpcDemoConductor} for the recommended approach.
 */
@Deprecated
@Conductor(namespace = "legacy")
@ProtocolAccess({ProtocolNames.HTTP})  // Only works well with HTTP
public class LegacyUserConductor {
    private static final Logger logger = LoggerFactory.getLogger(LegacyUserConductor.class);
    
    /**
     * Old way - HTTP-specific annotations.
     * This only works properly with HTTP protocol.
     */
    @Intent("get")
    @ProtocolAccess(schema = @ProtocolSchema(protocol = "HTTP", value = "GET /legacy/users/{id}"))
    public Map<String, Object> getUser(@PathParam("id") String userId) {
        // This will fail with gRPC and WebSocket!
        logger.warn("Using deprecated @PathParam annotation");
        return Map.of("userId", userId, "legacy", true);
    }
    
    /**
     * Old way - multiple HTTP-specific annotations.
     */
    @Intent("search")
    @ProtocolAccess(schema = @ProtocolSchema(protocol = "HTTP", value = "GET /legacy/users"))
    public Map<String, Object> searchUsers(
        @QueryParam("q") String query,
        @QueryParam(value = "limit", defaultValue = "10") int limit,
        @Header("X-Auth-Token") String token
    ) {
        logger.warn("Using deprecated HTTP-specific annotations");
        return Map.of(
            "query", query,
            "limit", limit,
            "hasAuth", token != null
        );
    }
    
    /**
     * Old way - RequestBody annotation.
     */
    @Intent("create")
    @ProtocolAccess(schema = @ProtocolSchema(protocol = "HTTP", value = "POST /legacy/users"))
    public Map<String, Object> createUser(@RequestBody Map<String, Object> request) {
        logger.warn("Using deprecated @RequestBody annotation");
        return Map.of(
            "created", true,
            "data", request
        );
    }
    
    /**
     * MIGRATION GUIDE:
     * 
     * Replace HTTP-specific annotations with @Param:
     * 
     * OLD:
     * @PathParam("id") String userId
     * @QueryParam("q") String query  
     * @Header("X-Auth") String token
     * @RequestBody UserDto user
     * 
     * NEW:
     * @Param("userId") String userId              // Auto-detected from path/query/body
     * @Param("query") String query                // Works across all protocols
     * @Param(value = "auth", hints = {"header"}) String token
     * @Param("name") String name                  // Extract individual fields
     * 
     * Benefits:
     * - Works with HTTP, gRPC, WebSocket automatically
     * - Smart parameter resolution
     * - No protocol coupling
     */
}
