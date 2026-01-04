package com.ubs.expensemanager.exception;

public class InvalidManagerRoleException extends RuntimeException {
    public InvalidManagerRoleException() {
        super("The specified user is not a manager");
    }
}
