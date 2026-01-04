package com.ubs.expensemanager.exception;

public class SelfManagerException extends RuntimeException {
    public SelfManagerException() {
        super("User cannot be their own manager");
    }
}
