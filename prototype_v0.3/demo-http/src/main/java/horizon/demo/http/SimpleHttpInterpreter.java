package horizon.demo.http;

import horizon.core.rendezvous.Interpreter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of Interpreter for HTTP requests.
 * This interpreter extracts the path as the intent key and creates a payload with
 * HTTP request details.
 */
public class SimpleHttpInterpreter implements Interpreter<SimpleHttpInput, String, Map<String, Object>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpInterpreter.class);

    /**
     * Extracts the intent key from the normalized input.
     * In this simple implementation, the path is used as the intent key.
     *
     * @param normalized the normalized input
     * @return the intent key (path)
     * @throws NullPointerException if normalized is null
     */
    @Override
    public String extractIntentKey(SimpleHttpInput normalized) {
        Objects.requireNonNull(normalized, "normalized must not be null");
        String path = normalized.getPath();
        LOGGER.debug("Extracted intent key (path): {}", path);
        return path;
    }

    /**
     * Extracts the intent payload from the normalized input.
     * In this simple implementation, a map with HTTP request details is created.
     *
     * @param normalized the normalized input
     * @return the intent payload (map with HTTP request details)
     * @throws NullPointerException if normalized is null
     */
    @Override
    public Map<String, Object> extractIntentPayload(SimpleHttpInput normalized) {
        Objects.requireNonNull(normalized, "normalized must not be null");
        
        // Create a map with HTTP request details
        Map<String, Object> payload = new HashMap<>();
        payload.put("path", normalized.getPath());
        payload.put("method", normalized.getMethod());
        payload.put("body", normalized.getBodyAsString());
        
        LOGGER.debug("Extracted intent payload for path: {}", normalized.getPath());
        return payload;
    }
}