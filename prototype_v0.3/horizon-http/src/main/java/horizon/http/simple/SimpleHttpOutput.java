package horizon.http.simple;

import horizon.core.model.RawOutput;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A simple implementation of RawOutput for HTTP responses.
 */
public class SimpleHttpOutput implements RawOutput {
    private final Object content;
    private final int statusCode;
    private final String contentType;

    /**
     * Creates a new SimpleHttpOutput with the specified parameters.
     *
     * @param content the content of the output
     * @param statusCode the status code of the output
     * @param contentType the content type of the output
     */
    public SimpleHttpOutput(Object content, int statusCode, String contentType) {
        this.content = content;
        this.statusCode = statusCode;
        this.contentType = Objects.requireNonNull(contentType, "contentType must not be null");
    }

    /**
     * Creates a new SimpleHttpOutput with the specified content and a 200 OK status code.
     *
     * @param content the content of the output
     * @param contentType the content type of the output
     */
    public SimpleHttpOutput(Object content, String contentType) {
        this(content, 200, contentType);
    }

    /**
     * Creates a new SimpleHttpOutput with the specified content, a 200 OK status code,
     * and a "text/plain" content type.
     *
     * @param content the content of the output
     */
    public SimpleHttpOutput(Object content) {
        this(content, 200, "text/plain");
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the content type of this output.
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the content as a string.
     * If the content is a byte array, it will be converted to a string using UTF-8 encoding.
     * Otherwise, the content's toString() method will be used.
     *
     * @return the content as a string
     */
    public String getContentAsString() {
        if (content instanceof byte[]) {
            return new String((byte[]) content, StandardCharsets.UTF_8);
        } else {
            return content != null ? content.toString() : "";
        }
    }

    @Override
    public String toString() {
        return "SimpleHttpOutput{" +
                "content=" + (content instanceof byte[] ? "..." : content) +
                ", statusCode=" + statusCode +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}