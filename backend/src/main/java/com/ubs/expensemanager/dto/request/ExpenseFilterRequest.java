package com.ubs.expensemanager.dto.request;

import com.ubs.expensemanager.model.ExpenseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class ExpenseFilterRequest {

    @Schema(description = "Filter by expense status", example = "PENDING")
    private ExpenseStatus status;

    @Schema(description = "Filter expenses from this date (inclusive)", example = "2026-01-01")
    private LocalDate startDate;

    @Schema(description = "Filter expenses to this date (inclusive)", example = "2026-01-31")
    private LocalDate endDate;

    @Schema(description = "Filter by expense category ID", example = "1")
    private Long expenseCategoryId;

    @Schema(description = "Filter by user ID (only accessible by MANAGER/FINANCE)", example = "1")
    private Long userId;
}
