package horizon.demo.dto;

import java.util.List;

/**
 * DTO for search user response.
 */
public class SearchUserResponse {
    private String query;
    private List<User> results;
    private int count;

    // Default constructor for Jackson
    public SearchUserResponse() {
    }

    public SearchUserResponse(String query, List<User> results) {
        this.query = query;
        this.results = results;
        this.count = results.size();
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}