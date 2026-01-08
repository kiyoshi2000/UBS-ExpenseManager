package com.ubs.expensemanager.service;

import com.ubs.expensemanager.mapper.UserMapper;
import com.ubs.expensemanager.security.JwtUtil;
import com.ubs.expensemanager.dto.request.LoginRequest;
import com.ubs.expensemanager.dto.request.UserCreateRequest;
import com.ubs.expensemanager.dto.response.LoginResponse;
import com.ubs.expensemanager.dto.response.UserResponse;
import com.ubs.expensemanager.exception.UserExistsException;
import com.ubs.expensemanager.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Service responsible for authentication logic.
 *
 * <p>Validates user credentials against the mock repository and handles
 * password verification. Avoid giving too specific reasons (e.g.:
 * login doesn't exist - that's insecure).</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    /**
     * Registers a new user in the system.
     *
     * <p>Creates a new user account with encrypted password and generates
     * an authentication token for immediate login after registration.</p>
     *
     * @param request the user registration data
     * @return LoginResponse containing token and user information
     * @throws UserExistsException if email is already registered
     */
    public LoginResponse register(UserCreateRequest request) {
        User savedUser = userService.createUser(request);

        String token = jwtUtil.generateToken(savedUser);

        return LoginResponse.builder()
                .token(token)
                .user(userMapper.toResponse(savedUser))
                .build();
    }

    /**
     * Authenticates a user with the provided email and password.
     *
     * <p>
     * This method uses the configured AuthenticationManager to validate the credentials.
     * If authentication succeeds, it generates a JWT token for the user and returns
     * a {@link LoginResponse} containing the token and user details.
     * </p>
     *
     * @param request the login request containing the user's email and password
     * @return a {@link LoginResponse} containing the JWT token and the authenticated user's details
     * @throws AuthenticationException if the credentials are invalid
     */
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .user(userMapper.toResponse(user))
                .build();
    }
}
