package horizon.core.flow.normalizer;

import horizon.core.constant.Scheme;
import horizon.core.model.input.RawInput;

import java.util.Map;

// 구조 변경 해야 할지도
public class NormalizedInput {

    private final Scheme scheme;
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final Object body;
    private final Object rawReference;

    /**
     * Constructs a NormalizedInput instance with the specified request components.
     *
     * @param scheme the protocol or scheme used for the request
     * @param method the HTTP method (e.g., GET or POST)
     * @param path the request path
     * @param headers the HTTP headers
     * @param queryParams the query parameters
     * @param body the request payload
     * @param rawReference a reference to the raw input data
     */
    public NormalizedInput(Scheme scheme, String method, String path, Map<String, String> headers, Map<String, String> queryParams, Object body, Object rawReference) {
        this.scheme = scheme;
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.queryParams = queryParams;
        this.body = body;
        this.rawReference = rawReference;
    }

    public String getScheme() {
        return scheme.name();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Object getBody() {
        return body;
    }

    public Object getRawReference() {
        return rawReference;
    }
}
