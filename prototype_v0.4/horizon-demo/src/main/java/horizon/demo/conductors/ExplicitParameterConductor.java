package horizon.demo.conductors;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates explicit parameter annotations including @RequestBody.
 * This conductor shows best practices for parameter declaration.
 */
@Conductor(namespace = "example")
public class ExplicitParameterConductor {
    private static final Logger logger = LoggerFactory.getLogger(ExplicitParameterConductor.class);
    
    /**
     * Example 1: Simple request body
     * Only the request body is needed.
     */
    @Intent("simple")
    @ProtocolAccess(
        schema = @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "POST /api/example/simple")
    )
    public Map<String, Object> simpleExample(@RequestBody Map<String, Object> data) {
        logger.info("Received data: {}", data);
        
        Map<String, Object> response = new HashMap<>();
        response.put("received", data);
        response.put("processed", true);
        return response;
    }
    
    /**
     * Example 2: Optional request body
     * The body might not be present.
     */
    @Intent("optional")
    @ProtocolAccess(
        schema = @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "POST /api/example/optional")
    )
    public Map<String, Object> optionalBody(
            @QueryParam("action") String action,
            @RequestBody(required = false) Map<String, Object> data
    ) {
        logger.info("Action: {}, Data present: {}", action, data != null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("action", action);
        response.put("hasData", data != null);
        if (data != null) {
            response.put("dataSize", data.size());
        }
        return response;
    }
    
    /**
     * Example 3: Multiple parameter sources
     * Clearly shows where each parameter comes from.
     */
    @Intent("complex")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.HTTP, 
            value = "PUT /api/example/items/{itemId}"
        )
    )
    public Map<String, Object> complexExample(
            @PathParam("itemId") Long itemId,
            @QueryParam("version") Integer version,
            @QueryParam(value = "dryRun", defaultValue = "false") Boolean dryRun,
            @Header("X-Request-ID") String requestId,
            @Header(value = "X-User-Agent", required = false) String userAgent,
            @RequestBody UpdateItemRequest updateRequest
    ) {
        logger.info("Complex example - itemId: {}, version: {}, dryRun: {}, requestId: {}", 
                   itemId, version, dryRun, requestId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("itemId", itemId);
        response.put("version", version);
        response.put("dryRun", dryRun);
        response.put("requestId", requestId);
        response.put("userAgent", userAgent);
        response.put("updateRequest", updateRequest);
        response.put("status", dryRun ? "simulated" : "updated");
        
        return response;
    }
    
    /**
     * Example 4: No request body
     * GET request with only query and path parameters.
     */
    @Intent("query")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.HTTP, 
            value = "GET /api/example/search/{category}"
        )
    )
    public Map<String, Object> queryExample(
            @PathParam("category") String category,
            @QueryParam("q") String query,
            @QueryParam(value = "page", defaultValue = "1") Integer page,
            @QueryParam(value = "size", defaultValue = "20") Integer size
    ) {
        logger.info("Search - category: {}, query: {}, page: {}, size: {}", 
                   category, query, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("category", category);
        response.put("query", query);
        response.put("page", page);
        response.put("size", size);
        response.put("results", new Object[]{
            Map.of("id", 1, "name", "Result 1"),
            Map.of("id", 2, "name", "Result 2")
        });
        
        return response;
    }
    
    /**
     * Example 5: Different body types
     * Shows that @RequestBody works with any type, not just Map.
     */
    @Intent("typed")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.HTTP, 
            value = "POST /api/example/typed"
        )
    )
    public CreateItemResponse typedExample(
            @Header("X-Correlation-ID") String correlationId,
            @RequestBody CreateItemRequest request
    ) {
        logger.info("Typed example - correlationId: {}, request: {}", correlationId, request);
        
        // Process the request
        Long newId = System.currentTimeMillis();
        
        CreateItemResponse response = new CreateItemResponse();
        response.setId(newId);
        response.setName(request.getName());
        response.setCategory(request.getCategory());
        response.setCreated(true);
        response.setCorrelationId(correlationId);
        
        return response;
    }
    
    // DTOs for typed examples
    public static class UpdateItemRequest {
        private String name;
        private String description;
        private Double price;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        @Override
        public String toString() {
            return "UpdateItemRequest{name='" + name + "', description='" + description + "', price=" + price + '}';
        }
    }
    
    public static class CreateItemRequest {
        private String name;
        private String category;
        private Double price;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        @Override
        public String toString() {
            return "CreateItemRequest{name='" + name + "', category='" + category + "', price=" + price + '}';
        }
    }
    
    public static class CreateItemResponse {
        private Long id;
        private String name;
        private String category;
        private boolean created;
        private String correlationId;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public boolean isCreated() { return created; }
        public void setCreated(boolean created) { this.created = created; }
        
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    }
}
