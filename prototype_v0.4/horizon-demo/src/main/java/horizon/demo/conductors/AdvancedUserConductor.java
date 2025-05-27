package horizon.demo.conductors;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Advanced User conductor demonstrating the new parameter annotation features.
 * This shows how to use @PathParam, @QueryParam, and @Header annotations.
 */
@Conductor(namespace = "advanced")
public class AdvancedUserConductor {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedUserConductor.class);
    
    /**
     * Example with multiple annotated parameters.
     * HTTP: GET /api/advanced/users/{userId}/profile?includeDetails=true
     * Headers: X-Auth-Token: xxx
     */
    @Intent("getUserProfile")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.HTTP, 
            value = "GET /api/advanced/users/{userId}/profile"
        )
    )
    public Map<String, Object> getUserProfile(
            @PathParam("userId") Long userId,
            @QueryParam("includeDetails") Boolean includeDetails,
            @Header("X-Auth-Token") String authToken
    ) {
        logger.info("Getting user profile - userId: {}, includeDetails: {}, authToken: {}", 
                   userId, includeDetails, authToken);
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", userId);
        profile.put("name", "Advanced User " + userId);
        profile.put("email", "user" + userId + "@advanced.example.com");
        
        if (Boolean.TRUE.equals(includeDetails)) {
            profile.put("details", Map.of(
                "joinDate", "2024-01-15",
                "lastLogin", "2024-12-20",
                "preferences", Map.of(
                    "theme", "dark",
                    "language", "en"
                )
            ));
        }
        
        profile.put("authenticated", authToken != null && !authToken.isEmpty());
        
        return profile;
    }
    
    /**
     * Example with optional query parameters and default values.
     * HTTP: GET /api/advanced/search?q=john&page=1&size=10
     */
    @Intent("searchUsers")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.HTTP,
            value = "GET /api/advanced/search"
        )
    )
    public Map<String, Object> searchUsers(
            @QueryParam("q") String query,
            @QueryParam(value = "page", defaultValue = "1") Integer page,
            @QueryParam(value = "size", defaultValue = "10") Integer size,
            @QueryParam(value = "sort", required = false) String sortBy
    ) {
        logger.info("Searching users - query: {}, page: {}, size: {}, sort: {}", 
                   query, page, size, sortBy);
        
        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("page", page);
        response.put("size", size);
        response.put("totalResults", 42);
        response.put("results", new Object[]{
            Map.of("id", 1, "name", "John Doe", "score", 0.95),
            Map.of("id", 2, "name", "Johnny Smith", "score", 0.87)
        });
        
        if (sortBy != null) {
            response.put("sortedBy", sortBy);
        }
        
        return response;
    }
    
    /**
     * Example mixing path params, query params, and request body.
     * HTTP: PUT /api/advanced/users/{userId}/settings?notify=true
     * Body: { "theme": "dark", "language": "ko" }
     */
    @Intent("updateUserSettings")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.HTTP,
            value = "PUT /api/advanced/users/{userId}/settings"
        )
    )
    public Map<String, Object> updateUserSettings(
            @PathParam("userId") Long userId,
            @QueryParam("notify") Boolean notify,
            Map<String, Object> settings  // This will be the request body
    ) {
        logger.info("Updating settings for user {} - notify: {}, settings: {}", 
                   userId, notify, settings);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("settings", settings);
        response.put("updated", true);
        
        if (Boolean.TRUE.equals(notify)) {
            response.put("notification", "Settings updated successfully!");
        }
        
        return response;
    }
    
    /**
     * Example with header-based content negotiation.
     * HTTP: POST /api/advanced/data
     * Headers: Content-Type: application/json, Accept: application/json
     */
    @Intent("processData")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.HTTP,
            value = "POST /api/advanced/data"
        )
    )
    public Object processData(
            @Header("Content-Type") String contentType,
            @Header("Accept") String acceptType,
            Map<String, Object> data
    ) {
        logger.info("Processing data - Content-Type: {}, Accept: {}", contentType, acceptType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("received", data);
        result.put("processedAt", System.currentTimeMillis());
        result.put("inputFormat", contentType);
        result.put("outputFormat", acceptType);
        
        // Different response based on Accept header
        if (acceptType != null && acceptType.contains("xml")) {
            result.put("format", "This would be XML in a real implementation");
        } else {
            result.put("format", "json");
        }
        
        return result;
    }
    
    /**
     * Complex path parameters example.
     * HTTP: GET /api/advanced/organizations/{orgId}/departments/{deptId}/employees/{empId}
     */
    @Intent("getEmployee")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.HTTP,
            value = "GET /api/advanced/organizations/{orgId}/departments/{deptId}/employees/{empId}"
        )
    )
    public Map<String, Object> getEmployee(
            @PathParam("orgId") Long orgId,
            @PathParam("deptId") Long deptId,
            @PathParam("empId") Long empId,
            @QueryParam(value = "includeHistory", defaultValue = "false") Boolean includeHistory
    ) {
        logger.info("Getting employee - org: {}, dept: {}, emp: {}, history: {}", 
                   orgId, deptId, empId, includeHistory);
        
        Map<String, Object> employee = new HashMap<>();
        employee.put("id", empId);
        employee.put("name", "Employee " + empId);
        employee.put("organizationId", orgId);
        employee.put("departmentId", deptId);
        
        if (includeHistory) {
            employee.put("history", new Object[]{
                Map.of("date", "2023-01-15", "event", "Joined"),
                Map.of("date", "2023-06-01", "event", "Promoted")
            });
        }
        
        return employee;
    }
}
