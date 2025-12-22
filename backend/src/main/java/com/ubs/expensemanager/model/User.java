package com.ubs.expensemanager.model;

import lombok.*;

/**
 * Represents an application user.
 *
 * <p>Holds authentication credentials and the associated {@link UserRole}
 * used for authorization.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String email;
    private String password;
    private UserRole role;
}
