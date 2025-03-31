package horizon.core.parser.normalizer;

import horizon.core.input.RawInput;

import java.util.Map;

public class NormalizedInput {

    private final RawInput.Scheme scheme;
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final Object body;
    private final Object rawReference;

    public NormalizedInput(RawInput.Scheme scheme, String method, String path, Map<String, String> headers, Map<String, String> queryParams, Object body, Object rawReference) {
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
