package com.ubs.expensemanager.exception;

/**
 * Exception thrown when an expense would exceed the daily or monthly budget limit.
 *
 * <p>Currently not used - the system logs warnings instead of throwing exceptions
 * when budgets are exceeded. This exception is reserved for future enhancement
 * when strict budget enforcement is required.</p>
 */
public class BudgetExceededException extends RuntimeException {

    public BudgetExceededException(String message) {
        super(message);
    }
}
