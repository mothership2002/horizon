package horizon.demo.conductors;

import horizon.core.annotation.Conductor;
import horizon.core.annotation.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Conductor for welcome-related operations.
 * This class demonstrates the use of the @Conductor and @Intent annotations.
 */
@Conductor(namespace = "")
public class WelcomeConductor {
    private static final Logger logger = LoggerFactory.getLogger(WelcomeConductor.class);
    
    /**
     * Provides a welcome message.
     *
     * @param payload the payload (not used)
     * @return a welcome message
     */
    @Intent("welcome")
    public Map<String, Object> welcome(Map<String, Object> payload) {
        logger.info("Generating welcome message");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Horizon Framework v0.4!");
        response.put("version", "0.4.0-SNAPSHOT");
        response.put("protocols", new String[]{"HTTP", "WebSocket"});
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
}