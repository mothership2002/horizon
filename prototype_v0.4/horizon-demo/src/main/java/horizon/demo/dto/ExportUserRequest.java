package horizon.demo.dto;

/**
 * DTO for exporting users.
 */
public class ExportUserRequest {
    private String format;

    // Default constructor for Jackson
    public ExportUserRequest() {
    }

    public ExportUserRequest(String format) {
        this.format = format;
    }

    // Getters and setters
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}