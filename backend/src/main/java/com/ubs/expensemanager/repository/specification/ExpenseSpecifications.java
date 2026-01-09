package com.ubs.expensemanager.repository.specification;

import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * JPA Specifications for filtering Expense entities.
 *
 * <p>This class provides reusable specifications that can be combined to build dynamic queries
 * for the Expense entity using Spring Data JPA Criteria API.</p>
 */
public class ExpenseSpecifications {

    /**
     * Creates a specification to filter expenses by status.
     *
     * @param status the expense status to filter by, or {@code null} to not apply this filter
     * @return a specification that matches expenses with the specified status, or {@code null} if status is {@code null}
     */
    public static Specification<Expense> withStatus(ExpenseStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    /**
     * Creates a specification to filter expenses from a start date (inclusive).
     *
     * @param startDate the start date to filter by, or {@code null} to not apply this filter
     * @return a specification that matches expenses on or after the start date, or {@code null} if startDate is {@code null}
     */
    public static Specification<Expense> withStartDate(LocalDate startDate) {
        return (root, query, cb) ->
                startDate == null ? null : cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate);
    }

    /**
     * Creates a specification to filter expenses to an end date (inclusive).
     *
     * @param endDate the end date to filter by, or {@code null} to not apply this filter
     * @return a specification that matches expenses on or before the end date, or {@code null} if endDate is {@code null}
     */
    public static Specification<Expense> withEndDate(LocalDate endDate) {
        return (root, query, cb) ->
                endDate == null ? null : cb.lessThanOrEqualTo(root.get("expenseDate"), endDate);
    }

    /**
     * Creates a specification to filter expenses by expense category ID.
     *
     * @param expenseCategoryId the expense category ID to filter by, or {@code null} to not apply this filter
     * @return a specification that matches expenses in the specified category, or {@code null} if expenseCategoryId is {@code null}
     */
    public static Specification<Expense> withExpenseCategoryId(Long expenseCategoryId) {
        return (root, query, cb) ->
                expenseCategoryId == null ? null : cb.equal(root.get("expenseCategory").get("id"), expenseCategoryId);
    }

    /**
     * Creates a specification to filter expenses by user ID.
     *
     * @param userId the user ID to filter by, or {@code null} to not apply this filter
     * @return a specification that matches expenses created by the specified user, or {@code null} if userId is {@code null}
     */
    public static Specification<Expense> withUserId(Long userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }
}
