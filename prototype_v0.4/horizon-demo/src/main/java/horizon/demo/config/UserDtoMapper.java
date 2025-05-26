package horizon.demo.config;

import horizon.demo.dto.*;
import horizon.web.http.dto.DtoMapper;

/**
 * Configures the DtoMapper with the DTO classes for the UserConductor intents.
 */
public class UserDtoMapper {
    
    /**
     * Configures the DtoMapper with the DTO classes for the UserConductor intents.
     * 
     * @param dtoMapper the DtoMapper to configure
     */
    public static void configure(DtoMapper dtoMapper) {
        // Register request DTOs
        dtoMapper.registerRequestDto("user.create", CreateUserRequest.class);
        dtoMapper.registerRequestDto("user.get", GetUserRequest.class);
        dtoMapper.registerRequestDto("user.update", UpdateUserRequest.class);
        dtoMapper.registerRequestDto("user.delete", DeleteUserRequest.class);
        dtoMapper.registerRequestDto("user.list", Object.class); // No parameters needed
        dtoMapper.registerRequestDto("user.search", SearchUserRequest.class);
        dtoMapper.registerRequestDto("user.bulkCreate", BulkCreateUserRequest.class);
        dtoMapper.registerRequestDto("user.import", ImportUserRequest.class);
        dtoMapper.registerRequestDto("user.export", ExportUserRequest.class);
        dtoMapper.registerRequestDto("user.validate", ValidateUserRequest.class);
    }
}