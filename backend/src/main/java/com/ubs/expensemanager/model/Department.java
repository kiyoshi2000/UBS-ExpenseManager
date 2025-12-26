package com.ubs.expensemanager.model;

import jakarta.persistence.*;
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
    */
    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * Default constructor required by JPA.
    */
    public Department() {
    }

    /**
     * Convenience constructor for creating Department instances.
     *
     * @param name           department name
     * @param dailyBudget    optional daily budget
     * @param monthlyBudget  mandatory monthly budget
     * @param currency       currency code
     */
    public Department(String name, BigDecimal dailyBudget, BigDecimal monthlyBudget, String currency) {
        this.name = name;
        this.dailyBudget = dailyBudget;
        this.monthlyBudget = monthlyBudget;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getDailyBudget() {
        return dailyBudget;
    }

    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }

    public String getCurrency() {
        return currency;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDailyBudget(BigDecimal dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
