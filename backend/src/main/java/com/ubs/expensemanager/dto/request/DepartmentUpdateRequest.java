package com.ubs.expensemanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO Request used to update an existing Department.
 *
 * <p>All fields are mandatory to keep the department data consistent.</p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentUpdateRequest {

    @Schema(description = "Department name", example = "Finance")
    @NotBlank(message = "department name is required")
    private String name;

    @Schema(description = "Monthly budget allocated to the department", example = "15000.00")
    @NotNull(message = "monthly budget is required")
    @PositiveOrZero(message = "monthly budget must be zero or positive")
    private BigDecimal monthlyBudget;

    @Schema(description = "Currency code (ISO 4217)", example = "USD")
    @NotBlank(message = "currency is required")
    private String currency;

    @Schema(description = "Daily budget allocated to the department", example = "700.00")
    @PositiveOrZero(message = "daily budget must be zero or positive")
    private BigDecimal dailyBudget;
}