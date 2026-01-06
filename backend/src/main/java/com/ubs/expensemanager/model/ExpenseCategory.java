package com.ubs.expensemanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import java.math.BigDecimal;

/**
 * Represents an Expense Category within the Expense Manager domain.
 *
 * This entity is mapped to the "expense_categories" table and stores
 * category information with daily and monthly budget limits.
 */
@Entity
@Table(
    name = "expense_categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_expense_categories_name", columnNames = "name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseCategory {

    /**
     * Primary identifier of the expense category.
     * Uses IDENTITY strategy to match the PostgreSQL BIGINT IDENTITY column.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the expense category (e.g., "Food", "Transport").
     * This field is mandatory and must be unique.
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Daily budget limit for this category.
     * This field is mandatory and must be a non-negative value.
     */
    @Column(name = "daily_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyBudget;

    /**
     * Monthly budget limit for this category.
     * This field is mandatory and must be a non-negative value.
     */
    @Column(name = "monthly_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyBudget;
}
