package com.ubs.expensemanager.exception;

/**
 * Exception thrown when a user attempts to access or modify an expense
 * they are not authorized to access.
 *
 * <p>For example, an EMPLOYEE attempting to view or update another employee's expense.</p>
 */
public class UnauthorizedExpenseAccessException extends RuntimeException {

    public UnauthorizedExpenseAccessException(String message) {
        super(message);
    }
}
