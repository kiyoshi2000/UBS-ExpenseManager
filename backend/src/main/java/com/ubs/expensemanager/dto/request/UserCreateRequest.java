package com.ubs.expensemanager.dto.request;

import com.ubs.expensemanager.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserCreateRequest {
    @NotBlank(message = "email is required")
    @Email
    @Schema(description = "Email of the user", example = "finance@ubs.com")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password must be at least 6 characters")
    @Schema(description = "Password of the user", example = "123456")
    private String password;

    @NotBlank(message = "name is required")
    @Schema(description = "Name of the user", example = "Pedro")
    private String name;

    @NotNull(message = "role is required")
    @Schema(description = "User role", example = "FINANCE")
    private UserRole role;

    @NotNull(message = "departmentId is required")
    @Schema(description = "Department ID", example = "1")
    private Long departmentId;

    @Email
    @Schema(description = "Email of the manager (optional, required only for EMPLOYEE role)",
            example = "manager@ubs.com")
    private String managerEmail;
}