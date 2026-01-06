package com.ubs.expensemanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO Request used to create a new Expense Category.
 *
 * <p>Contains the information required to register
 * an expense category in the system.</p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCategoryCreateRequest {

    @Schema(description = "Category name", example = "Food")
    @NotBlank(message = "category name is required")
    private String name;

    @Schema(description = "Daily budget limit for this category", example = "100.00")
    @NotNull(message = "daily budget is required")
    @PositiveOrZero(message = "daily budget must be zero or positive")
    private BigDecimal dailyBudget;

    @Schema(description = "Monthly budget limit for this category", example = "3000.00")
    @NotNull(message = "monthly budget is required")
    @PositiveOrZero(message = "monthly budget must be zero or positive")
    private BigDecimal monthlyBudget;
}
