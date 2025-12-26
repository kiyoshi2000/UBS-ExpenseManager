package com.ubs.expensemanager.exception;

public class ManagerHasSubordinatesException extends RuntimeException {
    public ManagerHasSubordinatesException() {
        super("Cannot deactivate manager with active subordinates." +
                " Please reassign or deactivate subordinates first.");
    }
}
