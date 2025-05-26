package horizon.demo.conductors;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Example conductor using the new unified @ProtocolAccess annotation.
 * This demonstrates the schema-based approach for protocol mapping.
 */
@Conductor(namespace = "product")
public class ProductConductor {
    private static final Logger logger = LoggerFactory.getLogger(ProductConductor.class);
    
    /**
     * Example using schema-based protocol access.
     * Both HTTP and WebSocket can access this method with different schemas.
     */
    @Intent("search")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /products/search"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "product.search")
        }
    )
    public Map<String, Object> searchProducts(Map<String, Object> payload) {
        logger.info("Searching products with: {}", payload);
        
        Map<String, Object> response = new HashMap<>();
        response.put("query", payload.get("q"));
        response.put("results", new Object[]{
            Map.of("id", 1, "name", "Product 1", "price", 99.99),
            Map.of("id", 2, "name", "Product 2", "price", 149.99)
        });
        response.put("count", 2);
        
        return response;
    }
    
    /**
     * HTTP-only endpoint using the new approach.
     */
    @Intent("export")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.HTTP, 
            value = "GET /products/export",
            attributes = {"contentType", "text/csv"}
        )
    )
    public Map<String, Object> exportProducts(Map<String, Object> payload) {
        logger.info("Exporting products");
        
        return Map.of(
            "format", payload.getOrDefault("format", "csv"),
            "count", 100,
            "status", "exported"
        );
    }
    
    /**
     * WebSocket-only streaming endpoint.
     */
    @Intent("subscribe")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.WEBSOCKET,
            value = "product.updates",
            attributes = {"streaming", "true"}
        )
    )
    public Map<String, Object> subscribeToUpdates(Map<String, Object> payload) {
        String sessionId = (String) payload.get("_sessionId");
        logger.info("Subscribing session {} to product updates", sessionId);
        
        return Map.of(
            "subscribed", true,
            "channel", "product.updates"
        );
    }
    
    /**
     * Example using convention-based routing (no explicit schema).
     * The framework will automatically generate routes based on the intent name.
     */
    @Intent("create")
    @ProtocolAccess({ProtocolNames.HTTP, ProtocolNames.WEBSOCKET})
    public Map<String, Object> createProduct(Map<String, Object> payload) {
        logger.info("Creating product: {}", payload);
        
        return Map.of(
            "id", System.currentTimeMillis(),
            "name", payload.get("name"),
            "price", payload.get("price"),
            "created", true
        );
    }
    
    /**
     * Example with multiple HTTP methods.
     */
    @Intent("update")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "PUT /products/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "PATCH /products/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "product.update")
        }
    )
    public Map<String, Object> updateProduct(Map<String, Object> payload) {
        Long id = extractId(payload);
        logger.info("Updating product {}: {}", id, payload);
        
        return Map.of(
            "id", id,
            "updated", true
        );
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
}
