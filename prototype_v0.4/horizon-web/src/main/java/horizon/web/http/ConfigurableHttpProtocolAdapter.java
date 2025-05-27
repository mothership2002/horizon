package horizon.web.http;

import horizon.core.ProtocolAggregator;
import horizon.core.protocol.IntentResolver;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * A configurable HTTP protocol adapter that allows custom intent resolution strategies.
 * This class extends HttpProtocolAdapter and adds support for custom intent resolvers.
 */
public class ConfigurableHttpProtocolAdapter extends HttpProtocolAdapter {
    private final List<IntentResolver<FullHttpRequest>> resolvers = new ArrayList<>();
    private final IntentResolver<FullHttpRequest> defaultResolver;

    public ConfigurableHttpProtocolAdapter() {
        // Default resolver uses the existing smart extraction logic
        this.defaultResolver = new DefaultHttpIntentResolver();
    }
    
    /**
     * Sets the protocol aggregator for accessing conductor metadata.
     * This enables automatic DTO conversion based on conductor method parameters.
     */
    @Override
    public void setProtocolAggregator(ProtocolAggregator aggregator) {
        super.setProtocolAggregator(aggregator);
    }

    /**
     * Adds a custom intent resolver with higher priority than the default.
     * Resolvers are tried in the order they are added, with the most recently added resolver having the highest priority.
     *
     * @param resolver the resolver to add
     */
    public void addResolver(IntentResolver<FullHttpRequest> resolver) {
        resolvers.add(0, resolver); // Add at beginning for priority
    }

    @Override
    protected String doExtractIntent(FullHttpRequest request) {
        // Try custom resolvers first
        for (IntentResolver<FullHttpRequest> resolver : resolvers) {
            if (resolver.canResolve(request)) {
                String intent = resolver.resolveIntent(request);
                if (intent != null) {
                    return intent;
                }
            }
        }

        // Fall back to default resolver
        return defaultResolver.resolveIntent(request);
    }

    /**
     * Default intent resolver that implements the smart REST-style mapping.
     */
    private static class DefaultHttpIntentResolver extends horizon.web.http.resolver.HttpIntentResolver {
        // Uses the common implementation from HttpIntentResolver
    }
}
