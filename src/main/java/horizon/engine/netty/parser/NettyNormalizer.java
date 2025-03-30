package horizon.engine.netty.parser;

import horizon.core.input.RawInput;
import horizon.core.parser.NormalizedInput;
import horizon.core.parser.ProtocolNormalizer;
import io.netty.handler.codec.http.FullHttpRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NettyNormalizer<HttpRawInput extends RawInput> implements ProtocolNormalizer<HttpRawInput> {

    @Override
    public NormalizedInput normalize(HttpRawInput rawInput) {
        FullHttpRequest request = (FullHttpRequest) rawInput.nativeRequest();
        URI uri = URI.create(request.uri());
        String method = request.method().name();
        String path = uri.getPath();
        Map<String, String> queryParams = parseQueryParams(uri);
        Map<String, String> headers = parseHeader(request);
        String body = request.content().toString(java.nio.charset.StandardCharsets.UTF_8);

        return new NormalizedInput(
                rawInput.getScheme(),
                method,
                path,
                headers,
                queryParams,
                body,
                rawInput.nativeRequest()
        );
    }

    private static Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> queryParams = new HashMap<>();

        if (uri.getQuery() != null) {
            for (String pair : uri.getQuery().split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) queryParams.put(kv[0], kv[1]);
                else if (kv.length == 1) queryParams.put(kv[0], "");
            }
        }
        return queryParams;
    }

    private Map<String, String> parseHeader(FullHttpRequest request) {
        return request.headers()
                .entries().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
