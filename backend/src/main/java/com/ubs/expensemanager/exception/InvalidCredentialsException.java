package com.ubs.expensemanager.exception;

/**
 * Exception thrown when a user provides invalid credentials during auth.
 *
 * <p>This exception is handled at {@link GlobalExceptionHandler}</p>
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
