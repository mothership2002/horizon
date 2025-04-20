package horizon.demo.http;

import horizon.core.model.HorizonContext;
import horizon.core.rendezvous.AbstractRendezvous;
import horizon.core.rendezvous.Sentinel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of Rendezvous for HTTP requests.
 * This class extends AbstractRendezvous to leverage its functionality.
 */
public class SimpleHttpRendezvous extends AbstractRendezvous<SimpleHttpInput, SimpleHttpInput, String, Map<String, Object>, SimpleHttpOutput> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpRendezvous.class);

    /**
     * Creates a new SimpleHttpRendezvous with default components.
     */
    public SimpleHttpRendezvous() {
        super(
            Collections.singletonList(new SimpleHttpSentinel()),
            new SimpleHttpNormalizer(),
            new SimpleHttpInterpreter()
        );
        LOGGER.info("SimpleHttpRendezvous initialized");
    }

    /**
     * Creates a new SimpleHttpRendezvous with custom components.
     *
     * @param sentinels the sentinels to use
     * @param normalizer the normalizer to use
     * @param interpreter the interpreter to use
     */
    public SimpleHttpRendezvous(
            List<Sentinel<SimpleHttpInput>> sentinels,
            SimpleHttpNormalizer normalizer,
            SimpleHttpInterpreter interpreter) {
        super(sentinels, normalizer, interpreter);
    }

    /**
     * Customizes the context before returning it from the encounter method.
     * This method is called by AbstractRendezvous.encounter().
     *
     * @param context the context to customize
     * @return the customized context
     */
    @Override
    protected HorizonContext customizeContext(HorizonContext context) {
        LOGGER.debug("Customizing context for HTTP request");
        return context;
    }

    /**
     * Gets the rendered output from the context.
     * This method is called by AbstractRendezvous.fallAway().
     *
     * @param context the context
     * @return the rendered output
     */
    @Override
    protected SimpleHttpOutput getRenderedOutput(HorizonContext context) {
        LOGGER.debug("Getting rendered output for HTTP response");

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

    /**
     * Customizes the output before returning it from the fallAway method.
     * This method is called by AbstractRendezvous.fallAway().
     *
     * @param output the output to customize
     * @param context the context
     * @return the customized output
     */
    @Override
    protected SimpleHttpOutput customizeOutput(SimpleHttpOutput output, HorizonContext context) {
        LOGGER.debug("Customizing output for HTTP response");
        return output;
    }
}
