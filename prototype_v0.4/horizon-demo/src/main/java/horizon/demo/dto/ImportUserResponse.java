package horizon.demo.dto;

/**
 * DTO for import user response.
 */
public class ImportUserResponse {
    private String message;
    private String source;
    private String format;
    private String status;

    // Default constructor for Jackson
    public ImportUserResponse() {
    }

    public ImportUserResponse(String message, String source, String format, String status) {
        this.message = message;
        this.source = source;
        this.format = format;
        this.status = status;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}