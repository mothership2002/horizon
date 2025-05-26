package horizon.demo.conductors;

import horizon.core.annotation.Conductor;
import horizon.core.annotation.Intent;
import horizon.core.annotation.ProtocolAccess;
import horizon.core.annotation.ProtocolSchema;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin conductor with HTTP-only access.
 * Demonstrates protocol restriction using the new @ProtocolAccess annotation.
 */
@Conductor(namespace = "admin")
public class AdminConductor {
    private static final Logger logger = LoggerFactory.getLogger(AdminConductor.class);
    
    @Intent("shutdown")
    @ProtocolAccess(
        schema = @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "POST /admin/shutdown")
    )
    public Map<String, Object> shutdown(Map<String, Object> payload) {
        logger.warn("Shutdown requested via admin endpoint");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "System shutdown initiated");
        response.put("delay", "30 seconds");
        
        // In real implementation, would trigger graceful shutdown
        return response;
    }
    
    @Intent("metrics")
    @ProtocolAccess(
        schema = @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /admin/metrics")
    )
    public Map<String, Object> getMetrics(Map<String, Object> payload) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("uptime", System.currentTimeMillis());
        metrics.put("memory", Runtime.getRuntime().totalMemory());
        metrics.put("threads", Thread.activeCount());
        
        return metrics;
    }
}
