package com.ubs.expensemanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    @Schema(description = "User email", example = "finance@ubs.com")
    private String email;
}
