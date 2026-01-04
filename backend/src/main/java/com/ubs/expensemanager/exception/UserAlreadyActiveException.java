package com.ubs.expensemanager.exception;

public class UserAlreadyActiveException extends RuntimeException {
    public UserAlreadyActiveException() {
        super("This user is already active");
    }
}
