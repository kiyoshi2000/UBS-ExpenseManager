package com.ubs.expensemanager.repository;

import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
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
    public static final List<User> USERS = List.of(
            new User("finance@ubs.com", new BCryptPasswordEncoder().encode("123456"), UserRole.FINANCE)
    );

    public static Optional<User> findByEmail(String email) {
        return USERS.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }
}
