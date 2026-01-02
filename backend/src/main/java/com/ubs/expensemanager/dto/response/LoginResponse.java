package com.ubs.expensemanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Response returned by {@link com.ubs.expensemanager.controller.AuthController}
 * after a successful login.
 *
 * <p><b>Deprecated:</b> This class is currently used only for login responses
 * and will be replaced in future by a complete response structure</p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class LoginResponse {
    @Schema(description = "User token")
    private String token;

    @Schema(description = "User info")
    private UserResponse user;
}
