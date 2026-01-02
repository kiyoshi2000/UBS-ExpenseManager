package com.ubs.expensemanager.model;

/**
 * Defines application user roles.
 */
public enum UserRole {
    FINANCE,
    MANAGER,
    EMPLOYEE;

    /**
     * Returns the role formatted as a Spring Security authority.
     *
     * @return authority string in the format {@code ROLE_<ROLE_NAME>}
     */
    public String asAuthority() {
        return "ROLE_" + name();
    }
}
