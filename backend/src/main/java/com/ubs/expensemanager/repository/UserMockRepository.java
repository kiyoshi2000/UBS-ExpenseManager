package com.ubs.expensemanager.repository;


import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

/**
 * In-memory repository for mock users, used for testing and initial development.
 *
 * <p><b>Deprecated:</b> provides basic lookup by email and predefines a static list of users.
 * This repository is temporary and will be removed when a real persistence
 * layer is implemented.</p>
 */
public class UserMockRepository {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserMock {
        private String email;
        private String password;
        private String role;
    }

    public static final List<UserMock> USERS = List.of(
            new UserMock("finance@ubs.com", new BCryptPasswordEncoder().encode("123"), "ROLE_FINANCE")
    );

    public static Optional<UserMock> findByEmail(String email) {
        return USERS.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }
}
