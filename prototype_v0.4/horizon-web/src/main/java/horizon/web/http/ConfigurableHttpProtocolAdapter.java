package horizon.web.http;

import horizon.core.ProtocolAggregator;
import horizon.core.protocol.IntentResolver;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * A configurable HTTP protocol adapter that allows custom intent resolution strategies.
 * This class extends HttpProtocolAdapter and adds support for custom intent resolvers.
 * 
 * The adapter allows registering multiple intent resolvers that will be tried in order
 * of priority before falling back to the default resolver.
 */
public class ConfigurableHttpProtocolAdapter extends HttpProtocolAdapter {
    private final List<IntentResolver<FullHttpRequest>> resolvers = new ArrayList<>();
    private final IntentResolver<FullHttpRequest> defaultResolver;

    /**
     * Creates a new configurable HTTP protocol adapter with a default resolver.
     * The default resolver uses the standard HTTP intent resolution logic.
     */
    public ConfigurableHttpProtocolAdapter() {
        // Initialize with the default resolver that uses the existing smart extraction logic
        this.defaultResolver = new DefaultHttpIntentResolver();
    }

    /**
     * Sets the protocol aggregator for accessing conductor metadata.
     * This enables automatic DTO conversion based on conductor method parameters.
     * 
     * The adapter uses the aggregator to access information about conductor methods,
     * such as parameter types, to properly convert HTTP request data to the expected
     * parameter types.
     *
     * @param aggregator the protocol aggregator instance
     */
    @Override
    public void setProtocolAggregator(ProtocolAggregator aggregator) {
        super.setProtocolAggregator(aggregator);
    }

    /**
     * Adds a custom intent resolver with higher priority than the default.
     * Resolvers are tried in the order of priority, with the most recently added resolver 
     * having the highest priority.
     *
     * @param resolver the resolver to add
     */
    public void addResolver(IntentResolver<FullHttpRequest> resolver) {
        resolvers.add(0, resolver); // Add at the beginning for highest priority
    }

    /**
     * Extracts the intent from an HTTP request by trying all registered resolvers
     * in order of priority, falling back to the default resolver if none match.
     *
     * @param request the HTTP request
     * @return the resolved intent string
     */
    @Override
    protected String doExtractIntent(FullHttpRequest request) {
        // Try custom resolvers first in order of priority
        for (IntentResolver<FullHttpRequest> resolver : resolvers) {
            String intent = resolver.resolveIntent(request);
            if (intent != null) {
                return intent;
            }
        }

        // Fall back to default resolver if no custom resolver matched
        return defaultResolver.resolveIntent(request);
    }

    /**
     * Default intent resolver that implements the smart REST-style mapping.
     * This resolver extends HttpIntentResolver to provide standard HTTP intent resolution
     * based on HTTP method and URI path patterns.
     * 
     * For example:
     * - GET /users/{id} → user.get
     * - POST /users → user.create
     * - PUT /users/{id} → user.update
     */
    private static class DefaultHttpIntentResolver extends horizon.web.http.resolver.HttpIntentResolver {
        // Uses the common implementation from HttpIntentResolver
    }
}
