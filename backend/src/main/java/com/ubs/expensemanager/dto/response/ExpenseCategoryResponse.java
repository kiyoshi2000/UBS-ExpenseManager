package com.ubs.expensemanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO Response representing an Expense Category returned by the API.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCategoryResponse {

    @Schema(description = "Category identifier", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Food")
    private String name;

    @Schema(description = "Daily budget limit for this category", example = "100.00")
    private BigDecimal dailyBudget;

    @Schema(description = "Monthly budget limit for this category", example = "3000.00")
    private BigDecimal monthlyBudget;
}
