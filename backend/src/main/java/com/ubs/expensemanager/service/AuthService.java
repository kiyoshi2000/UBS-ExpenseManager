package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.LoginRequest;
import com.ubs.expensemanager.dto.response.LoginResponse;
import com.ubs.expensemanager.exception.InvalidCredentialsException;
import com.ubs.expensemanager.repository.UserMockRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for authentication logic.
 *
 * <p>Validates user credentials against the mock repository and handles
 * password verification. Throws {@link InvalidCredentialsException} for
 * invalid email or password. Avoid giving too specific reasons (e.g.:
 * login doesn't exist - that's insecure).</p>
 */
@Service
@AllArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticates a user based on the provided login request.
     *
     * <p>Performs the following steps:
     * <ul>
     *     <li>Retrieves the user by email from the mock repository</li>
     *     <li>Validates the provided password against the stored hash (defined in
     *     {@link com.ubs.expensemanager.config.SecurityConfig}</li>
     *     <li>Returns a {@link LoginResponse} with the user's email if authentication succeeds</li>
     * </ul>
     *
     * @param request the login request containing email and password
     * @return a {@link LoginResponse} with the authenticated user's email
     * @throws InvalidCredentialsException if the email is not found or the password is incorrect
     */
    public LoginResponse authenticate(LoginRequest request) {

        UserMockRepository.UserMock user = UserMockRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid Credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        return new LoginResponse(user.getEmail());
    }
}
