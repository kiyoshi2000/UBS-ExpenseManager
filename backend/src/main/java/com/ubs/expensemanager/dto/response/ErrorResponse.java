package com.ubs.expensemanager.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a structured error response thrown at {@link com.ubs.expensemanager.exception.GlobalExceptionHandler}
 */
@Data
@AllArgsConstructor
@Builder
@Schema(description = "Error response structure")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred", example = "2025-12-20T15:04:06.098997078Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "Validation error")
    private String error;

    @Schema(description = "Error message", example = "Validation failed for one or more fields")
    private String message;

    @Schema(description = "Request path", example = "/api/auth/login")
    private String path;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Field-specific validation errors")
    private Map<String, String> errors;
}
