package horizon.demo.http;

import horizon.core.model.HorizonContext;
import horizon.core.rendezvous.Rendezvous;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of Rendezvous for HTTP requests.
 */
public class SimpleHttpRendezvous implements Rendezvous<SimpleHttpInput, SimpleHttpOutput> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpRendezvous.class);

    /**
     * Creates a context from the given raw input.
     *
     * @param input the raw input to process
     * @return a context initialized with data from the input
     * @throws IllegalArgumentException if the input is invalid
     * @throws NullPointerException if the input is null
     */
    @Override
    public HorizonContext encounter(SimpleHttpInput input) throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(input, "input must not be null");
        LOGGER.info("Received HTTP request: " + input.getMethod() + " " + input.getPath() + " from " + input.getSource());

        // Create a new context with the input
        HorizonContext context = new HorizonContext(input);

        // Store HTTP request details in the intent payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("path", input.getPath());
        payload.put("method", input.getMethod());
        payload.put("body", input.getBodyAsString());
        context.setIntentPayload(payload);

        return context;
    }

    /**
     * Finalizes the output from the given context.
     *
     * @param context the context containing the result
     * @return raw output containing the finalized result
     * @throws IllegalArgumentException if the context is invalid
     * @throws NullPointerException if the context is null
     */
    @Override
    public SimpleHttpOutput fallAway(HorizonContext context) throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(context, "context must not be null");

        // Check if the context has a failure cause
        if (context.getFailureCause() != null) {
            LOGGER.warn("Error processing request: {}", context.getFailureCause().getMessage());
            return new SimpleHttpOutput("Error: " + context.getFailureCause().getMessage(), 500, "text/plain");
        }

        // Get the payload from the context
        Map<String, Object> payload = (Map<String, Object>) context.getIntentPayload();
        if (payload == null) {
            LOGGER.warn("No payload found in context");
            return new SimpleHttpOutput("Internal Server Error: No payload found", 500, "text/plain");
        }

        // Get the path from the payload
        String path = (String) payload.get("path");

        // Generate a response based on the path
        if ("/".equals(path)) {
            return new SimpleHttpOutput("Welcome to the Horizon Framework HTTP Demo!", "text/plain");
        } else if ("/echo".equals(path)) {
            String body = (String) payload.get("body");
            return new SimpleHttpOutput(body, "text/plain");
        } else if ("/json".equals(path)) {
            return new SimpleHttpOutput("{\"message\":\"Hello, World!\"}", "application/json");
        } else if ("/flow".equals(path)) {
            // This endpoint demonstrates how the FlowEngine could be used
            // In a real implementation, this would be handled by the FlowEngine
            String body = (String) payload.get("body");
            String result = "Processed by FlowEngine simulation: " + (body != null ? body : "no content");
            return new SimpleHttpOutput(result, "text/plain");
        } else {
            return new SimpleHttpOutput("Not Found: " + path, 404, "text/plain");
        }
    }
}
