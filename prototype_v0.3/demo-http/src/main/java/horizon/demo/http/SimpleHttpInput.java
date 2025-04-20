package horizon.demo.http;

import horizon.core.model.RawInput;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * A simple implementation of RawInput for HTTP requests.
 */
public class SimpleHttpInput implements RawInput {
    private final String source;
    private final String scheme;
    private final byte[] body;
    private final String path;
    private final String method;

    /**
     * Creates a new SimpleHttpInput with the specified parameters.
     *
     * @param source the source of the input (e.g., the client's IP address)
     * @param scheme the scheme of the input (e.g., "http")
     * @param body the body of the input
     * @param path the path of the HTTP request
     * @param method the HTTP method (e.g., "GET", "POST")
     */
    public SimpleHttpInput(String source, String scheme, byte[] body, String path, String method) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.scheme = Objects.requireNonNull(scheme, "scheme must not be null");
        this.body = body != null ? Arrays.copyOf(body, body.length) : new byte[0];
        this.path = Objects.requireNonNull(path, "path must not be null");
        this.method = Objects.requireNonNull(method, "method must not be null");
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public byte[] getBody() {
        return Arrays.copyOf(body, body.length);
    }

    /**
     * Returns the path of the HTTP request.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the HTTP method.
     *
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Returns the body as a string using UTF-8 encoding.
     *
     * @return the body as a string
     */
    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "SimpleHttpInput{" +
                "source='" + source + '\'' +
                ", scheme='" + scheme + '\'' +
                ", body=" + (body.length > 100 ? "..." : getBodyAsString()) +
                ", path='" + path + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}