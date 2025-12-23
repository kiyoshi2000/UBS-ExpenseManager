package com.ubs.expensemanager.service;

import com.ubs.expensemanager.security.JwtUtil;
import com.ubs.expensemanager.dto.request.LoginRequest;
import com.ubs.expensemanager.dto.request.UserCreateRequest;
import com.ubs.expensemanager.dto.response.LoginResponse;
import com.ubs.expensemanager.dto.response.UserResponse;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.exception.UserExistsException;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
        if (repository.existsByEmail(request.getEmail())) {
            throw new UserExistsException(
                    "The email '" + request.getEmail() + "' is already registered"
            );
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User savedUser = repository.save(user);

        String token = jwtUtil.generateToken(savedUser);

        return LoginResponse.builder()
                .token(token)
                .user(UserResponse.fromEntity(savedUser))
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
                .user(UserResponse.fromEntity(user))
                .build();
    }

    public List<UserResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public UserResponse find(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no user with id " + id));
        return UserResponse.fromEntity(user);
    }

    public void delete(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no user with id " + id));
        repository.delete(user);
    }
}
