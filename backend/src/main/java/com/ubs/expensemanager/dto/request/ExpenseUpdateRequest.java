package com.ubs.expensemanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseUpdateRequest {

    @Schema(description = "Expense amount", example = "150.50")
    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    @Schema(description = "Expense description", example = "Team lunch at restaurant")
    @Size(max = 500, message = "description must not exceed 500 characters")
    private String description;

    @Schema(description = "Date when the expense occurred", example = "2026-01-08")
    @NotNull(message = "expense date is required")
    @PastOrPresent(message = "expense date cannot be in the future")
    private LocalDate expenseDate;

    @Schema(description = "Expense category identifier", example = "1")
    @NotNull(message = "expense category is required")
    private Long expenseCategoryId;

    @Schema(description = "Currency code (ISO 4217, 3 characters)", example = "USD")
    @NotBlank(message = "currency name is required")
    @Size(min = 3, max = 3, message = "currency name must be exactly 3 characters")
    private String currencyName;

    @Schema(description = "URL to expense receipt or proof", example = "https://example.com/receipts/12345.pdf")
    @Size(max = 1000, message = "receipt URL must not exceed 1000 characters")
    private String receiptUrl;
}
