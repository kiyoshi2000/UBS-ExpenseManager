package com.ubs.expensemanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.envers.Audited;
import java.math.BigDecimal;

/**
 * Represents a Department within the Expense Manager domain.
 *
 * This entity is mapped to the "departments" table and stores
 * both structural and financial information related to a department.
 */
@Entity
@Table(
    name = "departments",
    uniqueConstraints = {
        // Ensures department names are unique at the database level
        @UniqueConstraint(name = "uk_departments_name", columnNames = "name")
    }
)
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Department {

    /**
     * Primary identifier of the department.
     * Uses IDENTITY strategy to match the PostgreSQL BIGINT IDENTITY column.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the department.
     * This field is mandatory and must be unique.
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Optional daily budget allocated to the department.
     * When provided, it must be a non-negative value.
     */
    @Column(name = "daily_budget", precision = 15, scale = 2)
    private BigDecimal dailyBudget;

    /**
     * Monthly budget allocated to the department.
     * This field is mandatory and must be a non-negative value.
     */
    @Column(name = "monthly_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyBudget;

    /**
     * Currency used for the department's budget.
     * References the Currency entity to ensure referential integrity
     * and exchange-rate flexibility.
     */
    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;
}
