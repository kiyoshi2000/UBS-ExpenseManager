package com.ubs.expensemanager.config;

import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes basic application data on Spring Boot startup.
 *
 * <p> Creates predefined users with passwords encoded using the configured PasswordEncoder.
 * This is needed because bcrypt uses a random salt, so passwords cannot be reliably
 * inserted directly via SQL. </p>
 */
@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Method executed after Spring Boot initialization.
     * Creates predefined users if they do not already exist in the database.
     */
    @PostConstruct
    public void init() {
        List<User> users = List.of(
                User.builder()
                        .email("finance@ubs.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.FINANCE)
                        .build(),
                User.builder()
                        .email("manager@ubs.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.MANAGER)
                        .build(),
                User.builder()
                        .email("employee@ubs.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.EMPLOYEE)
                        .build()
        );

        // Save each user if it does not already exist
        for (User user : users) {
            userRepository.findByEmail(user.getEmail())
                    .orElseGet(() -> userRepository.save(user));
        }
    }
}
