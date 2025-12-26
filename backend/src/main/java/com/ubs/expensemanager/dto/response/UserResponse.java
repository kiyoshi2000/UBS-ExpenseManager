package com.ubs.expensemanager.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private String name;
    private boolean active;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ManagerInfo manager;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ManagerInfo {
        private Long id;
        private String name;
        private String email;
    }

    /**
     * Converts a User entity into a UserResponse DTO.
     * @param user the User entity
     * @return a UserResponse with role prefixed by "ROLE_"
     */
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role("ROLE_" + user.getRole().name())
                .manager(user.getManager() != null ? ManagerInfo.builder()
                        .id(user.getManager().getId())
                        .name(user.getManager().getName())
                        .email(user.getManager().getEmail())
                        .build() : null)
                .active(user.isActive())
                .build();
    }
}