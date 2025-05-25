package horizon.http;

import horizon.core.protocol.IntentResolver;
import horizon.core.protocol.ProtocolAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * A configurable HTTP protocol adapter that allows custom intent resolution strategies.
 */
public class ConfigurableHttpProtocolAdapter extends HttpProtocolAdapter {
    private final List<IntentResolver<FullHttpRequest>> resolvers = new ArrayList<>();
    private final IntentResolver<FullHttpRequest> defaultResolver;

    public ConfigurableHttpProtocolAdapter() {
        // Default resolver uses the existing smart extraction logic
        this.defaultResolver = new DefaultHttpIntentResolver();
    }

    /**
     * Adds a custom intent resolver with higher priority than the default.
     */
    public void addResolver(IntentResolver<FullHttpRequest> resolver) {
        resolvers.addFirst(resolver); // Add at beginning for priority
    }

    @Override
    public String extractIntent(FullHttpRequest request) {
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
    private static class DefaultHttpIntentResolver extends horizon.http.resolver.HttpIntentResolver {
        // Uses the common implementation from HttpIntentResolver
    }
}
