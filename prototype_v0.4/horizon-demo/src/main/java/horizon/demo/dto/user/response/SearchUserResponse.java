package horizon.demo.dto.user.response;

import horizon.demo.dto.user.User;

import java.util.List;

/**
 * DTO for search user response.
 */
public class SearchUserResponse {
    private String query;
    private List<User> results;

    // Default constructor for Jackson
    public SearchUserResponse() {
    }

    public SearchUserResponse(String query, List<User> results) {
        this.query = query;
        this.results = results;
    }

    // Getters and setters
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<User> getResults() {
        return results;
    }

    public void setResults(List<User> results) {
        this.results = results;
    }
}