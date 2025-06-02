package horizon.demo.conductor;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import horizon.demo.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User management conductor demonstrating protocol-neutral parameter handling with DTOs.
 * This single implementation works seamlessly with HTTP, WebSocket, and gRPC.
 */
@Conductor(namespace = "user")
@ProtocolAccess({ProtocolNames.HTTP, ProtocolNames.WEBSOCKET, ProtocolNames.GRPC})
public class UserConductor {
    private static final Logger logger = LoggerFactory.getLogger(UserConductor.class);

    // Simple in-memory storage for demo
    private final Map<String, UserData> users = new ConcurrentHashMap<>();

    /**
     * Creates a new user.
     * Protocol-neutral parameters automatically work with:
     * - HTTP: POST /users with JSON body
     * - WebSocket: {intent: "user.create", data: {...}}
     */
    @Intent("create")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "POST /users"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.create"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/CreateUser")
        }
    )
    public CreateUserResponse createUser(
        @Param("name") String name,
        @Param("email") String email
    ) {
        logger.info("Creating user: {} ({})", name, email);

        // Validation
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }

        String userId = UUID.randomUUID().toString();
        long createdAt = System.currentTimeMillis();

        UserData userData = new UserData(userId, name, email, createdAt);
        users.put(userId, userData);

        return new CreateUserResponse(userId, name, email, createdAt, true);
    }

    /**
     * Creates a new user using DTO.
     * This method demonstrates how to use DTOs for complex structures.
     * The framework will automatically convert the request to the DTO type.
     */
    @Intent("create.dto")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "POST /users/dto"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.create.dto"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/CreateUserDto")
        }
    )
    public CreateUserResponse createUserWithDto(CreateUserRequest request) {
        logger.info("Creating user with DTO: {}", request);

        // Validation
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        String name = request.getName();
        String email = request.getEmail();

        // Validation
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }

        String userId = UUID.randomUUID().toString();
        long createdAt = System.currentTimeMillis();

        UserData userData = new UserData(userId, name, email, createdAt);
        users.put(userId, userData);

        return new CreateUserResponse(userId, name, email, createdAt, true);
    }

    /**
     * Gets a user by ID.
     * @Param automatically finds userId from:
     * - HTTP: /users/{userId} or ?userId=xxx
     * - WebSocket: data.userId
     */
    @Intent("get")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /users/{userId}"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.get"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/GetUser")
        }
    )
    public GetUserResponse getUser(@Param("userId") String userId) {
        logger.info("Getting user: {}", userId);

        UserData userData = users.get(userId);
        if (userData == null) {
            return GetUserResponse.notFound();
        }

        return GetUserResponse.found(
            userData.userId(), 
            userData.name(), 
            userData.email(), 
            userData.createdAt()
        );
    }

    /**
     * Tests parameter resolution with multiple parameters.
     */
    @Intent("test.params")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /users/test"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.test.params"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/TestParams")
        }
    )
    public Map<String, Object> testParameterResolution(
        @Param("name") String name,
        @Param(value = "age", required = false, defaultValue = "25") Integer age,
        @Param(value = "active", required = false, defaultValue = "true") Boolean active,
        @Param(value = "tags", required = false) String[] tags
    ) {
        logger.info("Testing parameter resolution - name: {}, age: {}, active: {}, tags: {}", 
                   name, age, active, Arrays.toString(tags));

        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("age", age);
        result.put("active", active);
        result.put("tags", tags);
        result.put("timestamp", System.currentTimeMillis());
        result.put("success", true);

        return result;
    }

    /**
     * Tests complex parameter extraction from different sources.
     */
    @Intent("test.complex")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "POST /users/test-complex"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.test.complex"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/TestComplex")
        }
    )
    public Map<String, Object> testComplexParameters(
        @Param(value = "pathId", hints = {"path"}) String pathId,
        @Param(value = "queryParam", hints = {"query"}) String queryParam,
        @Param(value = "headerValue", hints = {"header"}) String headerValue,
        @Param(value = "bodyField", hints = {"body"}) String bodyField,
        @Param(value = "anyField") String anyField  // Should search everywhere
    ) {
        logger.info("Testing complex parameters - pathId: {}, queryParam: {}, headerValue: {}, bodyField: {}, anyField: {}", 
                   pathId, queryParam, headerValue, bodyField, anyField);

        Map<String, Object> result = new HashMap<>();
        result.put("pathId", pathId);
        result.put("queryParam", queryParam);
        result.put("headerValue", headerValue);
        result.put("bodyField", bodyField);
        result.put("anyField", anyField);
        result.put("resolvedAt", System.currentTimeMillis());

        return result;
    }

    /**
     * Lists all users.
     */
    @Intent("list")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /users"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.list"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/ListUsers")
        }
    )
    public Map<String, Object> listUsers(
        @Param(value = "limit", defaultValue = "10") int limit,
        @Param(value = "offset", defaultValue = "0") int offset
    ) {
        logger.info("Listing users - limit: {}, offset: {}", limit, offset);

        return Map.of(
            "users", users.values().stream()
                        .skip(offset)
                        .limit(limit)
                        .map(user -> Map.of(
                                "userId", (Object) user.userId(),
                                "name", user.name(),
                                "email", user.email(),
                                "createdAt", user.createdAt()
                        )).toList(),
            "total", users.size(),
            "limit", limit,
            "offset", offset,
            "hasMore", offset + users.size() < users.size()
        );
    }

    /**
     * Simple user data record for internal storage.
     */
    private record UserData(String userId, String name, String email, long createdAt) {}
}
