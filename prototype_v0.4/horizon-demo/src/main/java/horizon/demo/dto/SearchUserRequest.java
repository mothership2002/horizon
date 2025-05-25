package horizon.demo.dto;

/**
 * DTO for searching users.
 */
public class SearchUserRequest {
    private String q;
    private String searchBy;

    // Default constructor for Jackson
    public SearchUserRequest() {
    }

    public SearchUserRequest(String q, String searchBy) {
        this.q = q;
        this.searchBy = searchBy;
    }

    // Getters and setters
    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getSearchBy() {
        return searchBy;
    }

    public void setSearchBy(String searchBy) {
        this.searchBy = searchBy;
    }
}