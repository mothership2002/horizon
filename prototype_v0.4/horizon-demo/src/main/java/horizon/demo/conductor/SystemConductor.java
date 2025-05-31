package horizon.demo.conductor;

import horizon.core.annotation.*;
import horizon.core.metrics.MetricsCollector;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

/**
 * System conductor providing health checks, metrics, and system information.
 */
@Conductor(namespace = "system")
@ProtocolAccess({ProtocolNames.HTTP, ProtocolNames.WEBSOCKET})
public class SystemConductor {
    private static final Logger logger = LoggerFactory.getLogger(SystemConductor.class);
    
    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    
    /**
     * Health check endpoint.
     */
    @Intent("health")
    @ProtocolAccess(schema = @ProtocolSchema(protocol = "HTTP", value = "GET /health"))
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "timestamp", System.currentTimeMillis()
        );
    }
    
    /**
     * System information endpoint.
     */
    @Intent("info")
    @ProtocolAccess(schema = @ProtocolSchema(protocol = "HTTP", value = "GET /info"))
    public Map<String, Object> info() {
        return Map.of(
            "framework", "Horizon Framework",
            "version", "0.4",
            "java", Map.of(
                "version", System.getProperty("java.version"),
                "vendor", System.getProperty("java.vendor")
            ),
            "runtime", Map.of(
                "name", runtimeBean.getName(),
                "uptime", runtimeBean.getUptime(),
                "startTime", runtimeBean.getStartTime()
            )
        );
    }
    
    /**
     * Metrics endpoint.
     */
    @Intent("metrics")
    @ProtocolAccess(schema = @ProtocolSchema(protocol = "HTTP", value = "GET /metrics"))
    public Map<String, Object> metrics() {
        Map<String, Object> metrics = MetricsCollector.getInstance().getMetrics();
        
        // Add JVM metrics
        metrics.put("jvm.memory.used", memoryBean.getHeapMemoryUsage().getUsed());
        metrics.put("jvm.memory.max", memoryBean.getHeapMemoryUsage().getMax());
        metrics.put("jvm.threads", ManagementFactory.getThreadMXBean().getThreadCount());
        metrics.put("jvm.uptime", runtimeBean.getUptime());
        
        return metrics;
    }
    
    /**
     * Welcome message for root endpoint.
     */
    @Intent("welcome")
    @ProtocolAccess(schema = @ProtocolSchema(protocol = "HTTP", value = "GET /"))
    public Map<String, Object> welcome() {
        return Map.of(
            "message", "Welcome to Horizon Framework v0.4",
            "endpoints", Map.of(
                "health", "/health",
                "info", "/info",
                "metrics", "/metrics",
                "users", "/users"
            )
        );
    }
}
