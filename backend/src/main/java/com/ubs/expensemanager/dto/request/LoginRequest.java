package com.ubs.expensemanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank
    @Email
    @Schema(description = "User email", example = "finance@ubs.com")
    private String email;

    @Schema(description = "User password", example = "123")
    @NotBlank
    private String password;
}