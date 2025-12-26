package com.ubs.expensemanager.dto.request;

import com.ubs.expensemanager.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserUpdateRequest {
    @Email
    @Schema(description = "Email of the user", example = "finance@ubs.com")
    private String email;

    @Size(min = 6, message = "password must be at least 6 characters")
    @Schema(description = "Password of the user", example = "123456")
    private String password;

    @Schema(description = "User role", example = "FINANCE")
    private UserRole role;

    @Schema(description = "User name", example = "Carlos")
    private String name;

    @Email
    @Schema(description = "Email of the manager (optional, required only for EMPLOYEE role)",
            example = "manager@ubs.com")
    private String managerEmail;
}
