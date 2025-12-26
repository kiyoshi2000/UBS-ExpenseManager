package com.ubs.expensemanager.exception;

public class ManagerRequiredException extends RuntimeException {
    public ManagerRequiredException() {
        super("EMPLOYEE requires a manager");
    }
}
