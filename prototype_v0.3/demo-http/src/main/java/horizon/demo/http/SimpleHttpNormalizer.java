package horizon.demo.http;

import horizon.core.rendezvous.Normalizer;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of Normalizer for HTTP requests.
 * This normalizer simply passes through the SimpleHttpInput as the normalized form.
 */
public class SimpleHttpNormalizer implements Normalizer<SimpleHttpInput, SimpleHttpInput> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpNormalizer.class);

    /**
     * Normalizes the input.
     * In this simple implementation, the input is returned as-is.
     *
     * @param input the input to normalize
     * @return the normalized input
     * @throws NullPointerException if input is null
     */
    @Override
    public SimpleHttpInput normalize(SimpleHttpInput input) {
        Objects.requireNonNull(input, "input must not be null");
        LOGGER.debug("Normalizing HTTP request: {} {}", input.getMethod(), input.getPath());
        return input;
    }
}