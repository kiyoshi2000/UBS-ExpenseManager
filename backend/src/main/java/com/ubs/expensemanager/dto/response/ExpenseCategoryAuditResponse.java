package com.ubs.expensemanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO Response representing an audited version of an Expense Category.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCategoryAuditResponse {

    @Schema(description = "Category identifier", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Food")
    private String name;

    @Schema(description = "Daily budget limit for this category", example = "100.00")
    private BigDecimal dailyBudget;

    @Schema(description = "Monthly budget limit for this category", example = "3000.00")
    private BigDecimal monthlyBudget;

    @Schema(description = "Revision number", example = "1")
    private Number revisionNumber;

    @Schema(description = "Revision type (0=ADD, 1=MOD, 2=DEL)", example = "1")
    private Short revisionType;

    @Schema(description = "Revision timestamp", example = "2026-01-08T10:30:00")
    private LocalDateTime revisionDate;
}
