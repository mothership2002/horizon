package horizon.demo.dto;

/**
 * DTO for importing users from an external source.
 */
public class ImportUserRequest {
    private String source;
    private String format;

    // Default constructor for Jackson
    public ImportUserRequest() {
    }

    public ImportUserRequest(String source, String format) {
        this.source = source;
        this.format = format;
    }

    // Getters and setters
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}