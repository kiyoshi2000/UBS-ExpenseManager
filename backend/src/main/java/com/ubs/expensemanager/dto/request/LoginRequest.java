package com.ubs.expensemanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO Request for authentication.
 *
 * <p>Contains the user email and password. Validation annotations
 * ensure that both fields are valid and contains a valid email address</p>
 *
 * <p>This object is used as a request body at
 * {@link com.ubs.expensemanager.controller.AuthController#login(LoginRequest)}</p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email
    @Schema(description = "User email", example = "finance@ubs.com")
    private String email;

    @Schema(description = "User password", example = "123")
    @NotBlank(message = "Password is required")
    private String password;
}