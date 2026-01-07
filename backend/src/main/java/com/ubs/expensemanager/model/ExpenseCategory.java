package com.ubs.expensemanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

/**
 * Represents an Expense Category within the Expense Manager domain.
 *
 * <p> This entity is mapped to the "expense_categories" table and stores
 * category information with daily and monthly budget limits. </p>
 */
@Entity
@Table(
    name = "expense_categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_expense_categories_name", columnNames = "name")
    }
)
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "daily_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyBudget;

    @Column(name = "monthly_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyBudget;

    /**
     * Currency associated with this expense category.
     * All budget values (daily and monthly) are expressed in this currency.
     */
    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;
}
