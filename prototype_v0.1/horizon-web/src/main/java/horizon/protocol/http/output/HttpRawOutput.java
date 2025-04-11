package horizon.protocol.http.output;

import horizon.core.model.output.RawOutput;

import java.util.HashMap;
import java.util.Map;

public abstract class HttpRawOutput implements RawOutput {
    private int statusCode;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public HttpRawOutput() {}

    public HttpRawOutput(int statusCode, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers != null ? headers : new HashMap<>();
        this.body = body;
    }

    // 상태 코드 Getter/Setter
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
