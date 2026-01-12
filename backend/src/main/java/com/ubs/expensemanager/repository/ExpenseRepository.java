package com.ubs.expensemanager.repository;

import com.ubs.expensemanager.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Repository responsible for data access operations related to {@link Expense}.
 *
 * <p>This interface abstracts all persistence logic and provides
 * CRUD operations through Spring Data JPA, as well as custom queries
 * for budget validation and ownership checks.</p>
 */
@Repository
public interface ExpenseRepository extends
        JpaRepository<Expense, Long>,
        JpaSpecificationExecutor<Expense> {

    /**
     * Calculates the total expense amount for a user in a specific category on a specific date.
     * Excludes REJECTED expenses from the calculation.
     *
     * @param userId the user ID
     * @param categoryId the expense category ID
     * @param date the expense date
     * @return the sum of all expense amounts, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseCategory.id = :categoryId " +
           "AND e.expenseDate = :date " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByUserAndCategoryAndDate(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("date") LocalDate date
    );

    /**
     * Calculates the total expense amount for a user in a specific category on a specific date,
     * excluding a specific expense.
     * Excludes REJECTED expenses and the specified expense from the calculation.
     *
     * @param userId the user ID
     * @param categoryId the expense category ID
     * @param date the expense date
     * @param expenseId the expense ID to exclude from calculation
     * @return the sum of all expense amounts, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseCategory.id = :categoryId " +
           "AND e.expenseDate = :date " +
           "AND e.id != :expenseId " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByUserAndCategoryAndDateExcludingExpense(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("date") LocalDate date,
            @Param("expenseId") Long expenseId
    );

    /**
     * Calculates the total expense amount for a user in a specific category within a date range.
     * Excludes REJECTED expenses from the calculation.
     *
     * @param userId the user ID
     * @param categoryId the expense category ID
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return the sum of all expense amounts, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseCategory.id = :categoryId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByUserAndCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Calculates the total expense amount for a user in a specific category within a date range,
     * excluding a specific expense.
     * Excludes REJECTED expenses and the specified expense from the calculation.
     *
     * @param userId the user ID
     * @param categoryId the expense category ID
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @param expenseId the expense ID to exclude from calculation
     * @return the sum of all expense amounts, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseCategory.id = :categoryId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.id != :expenseId " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByUserAndCategoryAndDateRangeExcludingExpense(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("expenseId") Long expenseId
    );

    /**
     * Checks if an expense exists with the given ID and belongs to the specified user.
     * Used for ownership validation before updates or deletes.
     *
     * @param id the expense ID
     * @param userId the user ID
     * @return true if the expense exists and belongs to the user, false otherwise
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Calculates the total expense amount for a department on a specific date.
     * Excludes REJECTED expenses from the calculation.
     *
     * @param departmentId the department ID
     * @param date the expense date
     * @return the sum of all expense amounts, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.department.id = :departmentId " +
           "AND e.expenseDate = :date " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByDepartmentAndDate(
            @Param("departmentId") Long departmentId,
            @Param("date") LocalDate date
    );

    /**
     * Calculates the total expense amount for a department on a specific date,
     * excluding a specific expense.
     * Excludes REJECTED expenses and the specified expense from the calculation.
     *
     * @param departmentId the department ID
     * @param date the expense date
     * @param expenseId the expense ID to exclude from calculation
     * @return the sum of all expense amounts, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.department.id = :departmentId " +
           "AND e.expenseDate = :date " +
           "AND e.id != :expenseId " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByDepartmentAndDateExcludingExpense(
            @Param("departmentId") Long departmentId,
            @Param("date") LocalDate date,
            @Param("expenseId") Long expenseId
    );

    /**
     * Calculates the total expense amount for a department within a date range.
     * Excludes REJECTED expenses from the calculation.
     *
     * @param departmentId the department ID
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return the sum of all expense amounts, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.department.id = :departmentId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByDepartmentAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Calculates the total expense amount for a department within a date range,
     * excluding a specific expense.
     * Excludes REJECTED expenses and the specified expense from the calculation.
     *
     * @param departmentId the department ID
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @param expenseId the expense ID to exclude from calculation
     * @return the sum of all expense amounts, or 0 if no expenses found
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.department.id = :departmentId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.id != :expenseId " +
           "AND e.status != 'REJECTED'")
    BigDecimal sumAmountByDepartmentAndDateRangeExcludingExpense(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("expenseId") Long expenseId
    );
}
