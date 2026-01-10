package com.ubs.expensemanager.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private DepartmentInfo department;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ManagerInfo manager;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DepartmentInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ManagerInfo {
        private Long id;
        private String name;
        private String email;
    }

}