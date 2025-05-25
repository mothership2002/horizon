package horizon.demo.dto;

import java.util.List;

/**
 * DTO for export user response.
 */
public class ExportUserResponse {
    private String format;
    private List<User> users;
    private long exportedAt;
    private int count;

    // Default constructor for Jackson
    public ExportUserResponse() {
    }

    public ExportUserResponse(String format, List<User> users) {
        this.format = format;
        this.users = users;
        this.exportedAt = System.currentTimeMillis();
        this.count = users.size();
    }

    // Getters and setters
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public long getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(long exportedAt) {
        this.exportedAt = exportedAt;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}