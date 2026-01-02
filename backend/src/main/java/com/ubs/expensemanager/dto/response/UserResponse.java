package com.ubs.expensemanager.dto.response;

import com.ubs.expensemanager.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO representing a user for API responses.
 * Includes ID, email, and role (prefixed with "ROLE_").
 */
@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String role;

    /**
     * Converts a User entity into a UserResponse DTO.
     * @param user the User entity
     * @return a UserResponse with role prefixed by "ROLE_"
     */
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role("ROLE_" + user.getRole().name())
                .build();
    }
}