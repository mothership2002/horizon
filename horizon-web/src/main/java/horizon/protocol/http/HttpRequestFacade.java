package horizon.protocol.http;

import io.netty.handler.codec.http.FullHttpRequest;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public interface HttpRequestFacade {

    String getMethod();
    String getPath();
    String getBody();
    Map<String, String> getHeaders();
    Map<String, String> getQueryParams();

    class NettyHttpRequestFacade implements HttpRequestFacade {

        private final FullHttpRequest request;

        public NettyHttpRequestFacade(FullHttpRequest request) {
            this.request = request;
        }

        @Override
        public String getMethod() {
            return request.method().name();
        }

        @Override
        public String getPath() {
            return URI.create(request.uri()).getPath();
        }

        @Override
        public String getBody() {
            return request.content().toString(StandardCharsets.UTF_8);
        }

        @Override
        public Map<String, String> getHeaders() {
            return request.headers().entries()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        @Override
        public Map<String, String> getQueryParams() {
            Map<String, String> result = new HashMap<>();
            String query = URI.create(request.uri()).getQuery();
            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] kv = pair.split("=");
                    result.put(kv[0], kv.length > 1 ? kv[1] : "");
                }
            }
            return result;
        }
    }
}
