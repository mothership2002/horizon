package horizon.demo.conductors;

import horizon.core.annotation.Conductor;
import horizon.core.annotation.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * System information conductor.
 * Handles system-related intents like welcome, health, etc.
 */
@Conductor(namespace = "system")
public class SystemConductor {
    private static final Logger logger = LoggerFactory.getLogger(SystemConductor.class);
    
    @Intent(value = "", aliases = {"", "root", "home"})  // Handle empty intent
    public Map<String, Object> welcome(Map<String, Object> payload) {
        logger.info("Welcome intent received");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Horizon Framework v0.4!");
        response.put("version", "0.4.0-SNAPSHOT");
        response.put("features", new String[]{
            "Protocol Aggregation",
            "Annotation-based Conductors", 
            "Multi-protocol Support (HTTP, WebSocket)",
            "Intent-based Routing"
        });
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    @Intent("health")
    public Map<String, Object> health(Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("uptime", getUptime());
        response.put("memory", getMemoryInfo());
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    @Intent("info")
    public Map<String, Object> info(Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("framework", "Horizon");
        response.put("version", "0.4.0-SNAPSHOT");
        response.put("java", System.getProperty("java.version"));
        response.put("os", System.getProperty("os.name"));
        response.put("arch", System.getProperty("os.arch"));
        
        return response;
    }
    
    private long getUptime() {
        return System.currentTimeMillis() - startTime;
    }
    
    private Map<String, Object> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        memory.put("max", runtime.maxMemory());
        return memory;
    }
    
    private static final long startTime = System.currentTimeMillis();
}
