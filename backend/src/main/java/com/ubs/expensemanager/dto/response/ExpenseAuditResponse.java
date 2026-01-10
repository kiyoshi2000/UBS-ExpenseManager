package com.ubs.expensemanager.dto.response;

import com.ubs.expensemanager.model.ExpenseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO Response representing an audited version of an Expense.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseAuditResponse {

    @Schema(description = "Expense identifier", example = "1")
    private Long id;

    @Schema(description = "Expense amount", example = "150.50")
    private BigDecimal amount;

    @Schema(description = "Expense description", example = "Team lunch at restaurant")
    private String description;

    @Schema(description = "Date when the expense occurred", example = "2026-01-08")
    private LocalDate expenseDate;

    @Schema(description = "User identifier who created the expense", example = "1")
    private Long userId;

    @Schema(description = "User name who created the expense", example = "John Doe")
    private String userName;

    @Schema(description = "Expense category identifier", example = "1")
    private Long expenseCategoryId;

    @Schema(description = "Expense category name", example = "Food")
    private String expenseCategoryName;

    @Schema(description = "Currency code (ISO 4217)", example = "USD")
    private String currencyName;

    @Schema(description = "Exchange rate relative to USD", example = "1.000000")
    private BigDecimal exchangeRate;

    @Schema(description = "URL to expense receipt or proof", example = "https://example.com/receipts/12345.pdf")
    private String receiptUrl;

    @Schema(description = "Current status of the expense", example = "PENDING")
    private ExpenseStatus status;

    @Schema(description = "Revision number", example = "1")
    private Number revisionNumber;

    @Schema(description = "Revision type (0=ADD, 1=MOD, 2=DEL)", example = "1")
    private Short revisionType;

    @Schema(description = "Revision timestamp", example = "2026-01-08T10:30:00")
    private LocalDateTime revisionDate;
}
