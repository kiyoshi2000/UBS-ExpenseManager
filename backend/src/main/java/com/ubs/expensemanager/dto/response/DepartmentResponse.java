package com.ubs.expensemanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;


/**
 * DTO Response representing a Department returned by the API.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentResponse {

    @Schema(description = "Department identifier", example = "1")
    private Long id;

    @Schema(description = "Department name", example = "Finance")
    private String name;

    @Schema(description = "Monthly budget allocated to the department", example = "10000.00")
    private BigDecimal monthlyBudget;

    @Schema(description = "Currency code (ISO 4217)", example = "USD")
    private String currency;

    @Schema(description = "Daily budget allocated to the department", example = "500.00")
    private BigDecimal dailyBudget;
}