package horizon.demo.conductors;

import horizon.demo.dto.user.User;
import horizon.demo.dto.user.request.*;
import horizon.demo.dto.user.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the UserConductor class.
 * This test class is organized into nested classes for each operation
 * to improve readability and maintainability.
 */
class UserConductorTest {

    private UserConductor userConductor;

    @BeforeEach
    void setUp() {
        userConductor = new UserConductor();
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create a user with valid data")
        void shouldCreateUserWithValidData() {
            // Given
            CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com");

            // When
            User user = userConductor.createUser(request);

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getId()).isNotNull();
            assertThat(user.getName()).isEqualTo("John Doe");
            assertThat(user.getEmail()).isEqualTo("john@example.com");
            assertThat(user.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when name is empty")
        void shouldThrowExceptionWhenNameIsEmpty() {
            // Given
            CreateUserRequest request = new CreateUserRequest("", "john@example.com");

            // When/Then
            assertThatThrownBy(() -> userConductor.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name is required");
        }

        @Test
        @DisplayName("Should throw exception when email is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            // Given
            CreateUserRequest request = new CreateUserRequest("John Doe", "invalid-email");

            // When/Then
            assertThatThrownBy(() -> userConductor.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valid email is required");
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        private User createdUser;

        @BeforeEach
        void setUp() {
            // Create a test user
            CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com");
            createdUser = userConductor.createUser(request);
        }

        @Test
        @DisplayName("Should get user by ID")
        void shouldGetUserById() {
            // Given
            GetUserRequest request = new GetUserRequest(createdUser.getId());

            // When
            User user = userConductor.getUser(request);

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(createdUser.getId());
            assertThat(user.getName()).isEqualTo("John Doe");
            assertThat(user.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should throw exception when user ID is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // Given
            GetUserRequest request = new GetUserRequest(null);

            // When/Then
            assertThatThrownBy(() -> userConductor.getUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");
        }

        @Test
        @DisplayName("Should throw exception when user is not found")
        void shouldThrowExceptionWhenUserIsNotFound() {
            // Given
            GetUserRequest request = new GetUserRequest(9999L);

            // When/Then
            assertThatThrownBy(() -> userConductor.getUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: 9999");
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        private User createdUser;

        @BeforeEach
        void setUp() {
            // Create a test user
            CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com");
            createdUser = userConductor.createUser(request);
        }

        @Test
        @DisplayName("Should update user with valid data")
        void shouldUpdateUserWithValidData() {
            // Given
            UpdateUserRequest request = new UpdateUserRequest();
            request.setId(createdUser.getId());
            request.setName("Jane Doe");
            request.setEmail("jane@example.com");

            // When
            User updatedUser = userConductor.updateUser(request);

            // Then
            assertThat(updatedUser).isNotNull();
            assertThat(updatedUser.getId()).isEqualTo(createdUser.getId());
            assertThat(updatedUser.getName()).isEqualTo("Jane Doe");
            assertThat(updatedUser.getEmail()).isEqualTo("jane@example.com");
            assertThat(updatedUser.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when email is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            // Given
            UpdateUserRequest request = new UpdateUserRequest();
            request.setId(createdUser.getId());
            request.setEmail("invalid-email");

            // When/Then
            assertThatThrownBy(() -> userConductor.updateUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valid email is required");
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        private User createdUser;

        @BeforeEach
        void setUp() {
            // Create a test user
            CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com");
            createdUser = userConductor.createUser(request);
        }

        @Test
        @DisplayName("Should delete user by ID")
        void shouldDeleteUserById() {
            // Given
            DeleteUserRequest request = new DeleteUserRequest(createdUser.getId());

            // When
            DeleteUserResponse response = userConductor.deleteUser(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getId()).isEqualTo(createdUser.getId());

            // Verify user is deleted
            GetUserRequest getRequest = new GetUserRequest(createdUser.getId());
            assertThatThrownBy(() -> userConductor.getUser(getRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: " + createdUser.getId());
        }
    }

    @Nested
    @DisplayName("Bulk Create Users Tests")
    class BulkCreateUsersTests {

        @Test
        @DisplayName("Should bulk create users with valid data")
        void shouldBulkCreateUsersWithValidData() {
            // Given
            CreateUserRequest request1 = new CreateUserRequest("John Doe", "john@example.com");
            CreateUserRequest request2 = new CreateUserRequest("Jane Doe", "jane@example.com");
            BulkCreateUserRequest request = new BulkCreateUserRequest();
            request.setUsers(Arrays.asList(request1, request2));

            // When
            BulkCreateUserResponse response = userConductor.bulkCreateUsers(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUsers()).hasSize(2);
            assertThat(response.getUsers().get(0).getName()).isEqualTo("John Doe");
            assertThat(response.getUsers().get(1).getName()).isEqualTo("Jane Doe");
        }
    }

    @Nested
    @DisplayName("List Users Tests")
    class ListUsersTests {

        @BeforeEach
        void setUp() {
            // Create some test users
            userConductor.createUser(new CreateUserRequest("John Doe", "john@example.com"));
            userConductor.createUser(new CreateUserRequest("Jane Doe", "jane@example.com"));
        }

        @Test
        @DisplayName("Should list all users")
        void shouldListAllUsers() {
            // When
            UserListResponse response = userConductor.listUsers(null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUsers()).isNotEmpty();
            assertThat(response.getUsers()).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Search Users Tests")
    class SearchUsersTests {

        @BeforeEach
        void setUp() {
            // Create some test users
            userConductor.createUser(new CreateUserRequest("John Doe", "john@example.com"));
            userConductor.createUser(new CreateUserRequest("Jane Doe", "jane@example.com"));
            userConductor.createUser(new CreateUserRequest("Bob Smith", "bob@example.com"));
        }

        @Test
        @DisplayName("Should search users by name")
        void shouldSearchUsersByName() {
            // Given
            SearchUserRequest request = new SearchUserRequest();
            request.setQ("Doe");
            request.setSearchBy("name");

            // When
            SearchUserResponse response = userConductor.searchUsers(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getResults()).hasSize(2);
            assertThat(response.getResults().stream().map(User::getName))
                .allMatch(name -> name.contains("Doe"));
        }

        @Test
        @DisplayName("Should search users by email")
        void shouldSearchUsersByEmail() {
            // Given
            SearchUserRequest request = new SearchUserRequest();
            request.setQ("bob");
            request.setSearchBy("email");

            // When
            SearchUserResponse response = userConductor.searchUsers(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getResults()).hasSize(1);
            assertThat(response.getResults().get(0).getEmail()).isEqualTo("bob@example.com");
        }
    }
}
