package horizon.demo.http;

import horizon.core.model.HorizonContext;
import horizon.core.rendezvous.Sentinel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of Sentinel for HTTP requests.
 * This sentinel logs information about incoming requests and outgoing responses.
 */
public class SimpleHttpSentinel implements Sentinel<SimpleHttpInput> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpSentinel.class);

    /**
     * Inspects the inbound input.
     * In this simple implementation, it logs information about the HTTP request.
     *
     * @param input the input to inspect
     */
    @Override
    public void inspectInbound(SimpleHttpInput input) {
        LOGGER.info("Inbound HTTP request: {} {} from {}", 
                input.getMethod(), 
                input.getPath(), 
                input.getSource());
    }

    /**
     * Inspects the outbound context.
     * In this simple implementation, it logs information about the HTTP response.
     *
     * @param context the context to inspect
     */
    @Override
    public void inspectOutbound(HorizonContext context) {
        Object renderedOutput = context.getRenderedOutput();
        if (renderedOutput instanceof SimpleHttpOutput) {
            SimpleHttpOutput output = (SimpleHttpOutput) renderedOutput;
            LOGGER.info("Outbound HTTP response: status={}, contentType={}", 
                    output.getStatusCode(), 
                    output.getContentType());
        } else {
            LOGGER.info("Outbound context processed: {}", context.getTraceId());
        }
    }
}