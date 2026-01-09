package com.ubs.expensemanager.exception;

/**
 * Exception thrown when an invalid expense status transition is attempted.
 *
 * <p>For example, attempting to approve an expense that is not in the correct status,
 * or trying to transition from REJECTED to APPROVED.</p>
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
