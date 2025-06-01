package horizon.demo.conductor;

import horizon.core.annotation.*;
import horizon.core.metrics.MetricsCollector;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * System conductor providing health checks, metrics, and system information.
 * Accessible via HTTP and WebSocket for monitoring purposes.
 */
@Conductor(namespace = "system")
@ProtocolAccess({ProtocolNames.HTTP, ProtocolNames.WEBSOCKET})
public class SystemConductor {
    private static final Logger logger = LoggerFactory.getLogger(SystemConductor.class);
    
    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final long startTime = System.currentTimeMillis();
    
    /**
     * Health check endpoint.
     * Protocol mappings:
     * - HTTP: GET /health
     * - WebSocket: system.health
     */
    @Intent("health")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /health"),
            @ProtocolSchema(protocol = "WebSocket", value = "system.health")
        }
    )
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("uptime", getUptime());
        
        // Check various subsystems
        Map<String, String> checks = new HashMap<>();
        checks.put("memory", checkMemory());
        checks.put("protocols", checkProtocols());
        response.put("checks", checks);
        
        return response;
    }
    
    /**
     * System information endpoint.
     * Protocol mappings:
     * - HTTP: GET /info
     * - WebSocket: system.info
     */
    @Intent("info")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /info"),
            @ProtocolSchema(protocol = "WebSocket", value = "system.info")
        }
    )
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        
        // Framework info
        Map<String, Object> framework = new HashMap<>();
        framework.put("name", "Horizon Framework");
        framework.put("version", "0.4.0");
        framework.put("description", "Protocol Aggregation Framework");
        response.put("framework", framework);
        
        // Java info
        Map<String, Object> java = new HashMap<>();
        java.put("version", System.getProperty("java.version"));
        java.put("vendor", System.getProperty("java.vendor"));
        java.put("home", System.getProperty("java.home"));
        response.put("java", java);
        
        // Runtime info
        Map<String, Object> runtime = new HashMap<>();
        runtime.put("name", runtimeBean.getName());
        runtime.put("uptime", runtimeBean.getUptime());
        runtime.put("startTime", runtimeBean.getStartTime());
        runtime.put("vmName", runtimeBean.getVmName());
        runtime.put("vmVersion", runtimeBean.getVmVersion());
        response.put("runtime", runtime);
        
        // OS info
        Map<String, Object> os = new HashMap<>();
        os.put("name", System.getProperty("os.name"));
        os.put("version", System.getProperty("os.version"));
        os.put("arch", System.getProperty("os.arch"));
        os.put("processors", Runtime.getRuntime().availableProcessors());
        response.put("os", os);
        
        return response;
    }
    
    /**
     * Metrics endpoint.
     * Protocol mappings:
     * - HTTP: GET /metrics
     * - WebSocket: system.metrics
     */
    @Intent("metrics")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /metrics"),
            @ProtocolSchema(protocol = "WebSocket", value = "system.metrics")
        }
    )
    public Map<String, Object> metrics(
        @Param(value = "detailed", defaultValue = "false") boolean detailed
    ) {
        Map<String, Object> response = new HashMap<>();
        
        // Framework metrics
        Map<String, Object> frameworkMetrics = MetricsCollector.getInstance().getMetrics();
        response.put("framework", frameworkMetrics);
        
        // JVM metrics
        Map<String, Object> jvmMetrics = new HashMap<>();
        
        // Memory metrics
        Map<String, Object> memory = new HashMap<>();
        memory.put("heap.used", memoryBean.getHeapMemoryUsage().getUsed());
        memory.put("heap.max", memoryBean.getHeapMemoryUsage().getMax());
        memory.put("heap.committed", memoryBean.getHeapMemoryUsage().getCommitted());
        memory.put("nonheap.used", memoryBean.getNonHeapMemoryUsage().getUsed());
        jvmMetrics.put("memory", memory);
        
        // Thread metrics
        Map<String, Object> threads = new HashMap<>();
        threads.put("count", ManagementFactory.getThreadMXBean().getThreadCount());
        threads.put("peak", ManagementFactory.getThreadMXBean().getPeakThreadCount());
        threads.put("daemon", ManagementFactory.getThreadMXBean().getDaemonThreadCount());
        jvmMetrics.put("threads", threads);
        
        // GC metrics
        if (detailed) {
            Map<String, Object> gc = new HashMap<>();
            ManagementFactory.getGarbageCollectorMXBeans().forEach(gcBean -> {
                Map<String, Object> gcInfo = new HashMap<>();
                gcInfo.put("count", gcBean.getCollectionCount());
                gcInfo.put("time", gcBean.getCollectionTime());
                gc.put(gcBean.getName(), gcInfo);
            });
            jvmMetrics.put("gc", gc);
        }
        
        response.put("jvm", jvmMetrics);
        response.put("timestamp", Instant.now().toString());
        
        return response;
    }
    
    /**
     * Welcome/root endpoint.
     * Protocol mappings:
     * - HTTP: GET /
     * - WebSocket: system.welcome
     */
    @Intent("welcome")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /"),
            @ProtocolSchema(protocol = "WebSocket", value = "system.welcome")
        }
    )
    public Map<String, Object> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Horizon Framework v0.4");
        response.put("description", "A protocol aggregation framework for building multi-protocol services");
        
        // Available endpoints
        Map<String, Object> endpoints = new HashMap<>();
        
        Map<String, String> system = new HashMap<>();
        system.put("health", "/health - Health check");
        system.put("info", "/info - System information");
        system.put("metrics", "/metrics - Performance metrics");
        endpoints.put("system", system);
        
        Map<String, String> user = new HashMap<>();
        user.put("create", "POST /users - Create user");
        user.put("get", "GET /users/{id} - Get user");
        user.put("update", "PUT /users/{id} - Update user");
        user.put("delete", "DELETE /users/{id} - Delete user");
        user.put("list", "GET /users - List users");
        user.put("validate", "POST /users/validate - Validate user data");
        endpoints.put("user", user);
        
        response.put("endpoints", endpoints);
        
        // Supported protocols
        Map<String, Object> protocols = new HashMap<>();
        protocols.put("http", "Port 8080");
        protocols.put("websocket", "Port 8081 - ws://localhost:8081/ws");
        protocols.put("grpc", "Port 9090");
        response.put("protocols", protocols);
        
        return response;
    }
    
    /**
     * Reset metrics endpoint.
     * Protocol mappings:
     * - HTTP: POST /metrics/reset
     * - WebSocket: system.metrics.reset
     */
    @Intent("metrics.reset")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "POST /metrics/reset"),
            @ProtocolSchema(protocol = "WebSocket", value = "system.metrics.reset")
        }
    )
    public Map<String, Object> resetMetrics(
        @Param(value = "confirm", defaultValue = "false") boolean confirm
    ) {
        Map<String, Object> response = new HashMap<>();
        
        if (!confirm) {
            response.put("success", false);
            response.put("message", "Metrics reset requires confirmation. Set 'confirm' to true.");
            return response;
        }
        
        MetricsCollector.getInstance().reset();
        
        response.put("success", true);
        response.put("message", "Metrics have been reset");
        response.put("timestamp", Instant.now().toString());
        
        return response;
    }
    
    // Helper methods
    
    private long getUptime() {
        return System.currentTimeMillis() - startTime;
    }
    
    private String checkMemory() {
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        double usagePercent = (double) usedMemory / maxMemory * 100;
        
        if (usagePercent > 90) {
            return "CRITICAL";
        } else if (usagePercent > 70) {
            return "WARNING";
        } else {
            return "OK";
        }
    }
    
    private String checkProtocols() {
        // In a real implementation, would check if all protocols are responsive
        return "OK";
    }
}
