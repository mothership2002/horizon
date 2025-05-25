package horizon.demo.conductors;

import horizon.core.annotation.Conductor;
import horizon.core.annotation.Intent;
import horizon.core.annotation.HttpResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin conductor with HTTP-only access.
 * No WebSocket mappings = no WebSocket access.
 */
@Conductor(namespace = "admin")
public class AdminConductor {
    private static final Logger logger = LoggerFactory.getLogger(AdminConductor.class);
    
    @Intent("shutdown")
    @HttpResource("POST /admin/shutdown")  // Only HTTP mapping = HTTP only access
    public Map<String, Object> shutdown(Map<String, Object> payload) {
        logger.warn("Shutdown requested via admin endpoint");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "System shutdown initiated");
        response.put("delay", "30 seconds");
        
        // In real implementation, would trigger graceful shutdown
        return response;
    }
    
    @Intent("metrics")
    @HttpResource("GET /admin/metrics")  // Only HTTP mapping = HTTP only access
    public Map<String, Object> getMetrics(Map<String, Object> payload) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("uptime", System.currentTimeMillis());
        metrics.put("memory", Runtime.getRuntime().totalMemory());
        metrics.put("threads", Thread.activeCount());
        
        return metrics;
    }
}
