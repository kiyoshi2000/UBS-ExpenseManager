package com.ubs.expensemanager.model;

/**
 * Defines the status of an expense in the approval workflow.
 */
public enum ExpenseStatus {
    /**
     * Expense is awaiting initial approval from a manager.
     */
    PENDING,

    /**
     * Expense has been approved by a manager and is awaiting finance approval.
     */
    APPROVED_BY_MANAGER,

    /**
     * Expense has been fully approved by finance and is ready for reimbursement.
     */
    APPROVED_BY_FINANCE,

    /**
     * Expense has been rejected and will not be reimbursed.
     */
    REJECTED,

    /**
     * Expense requires revision before it can be approved.
     */
    REQUIRES_REVISION
}
