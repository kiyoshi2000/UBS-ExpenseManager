package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.request.LoginRequest;
import com.ubs.expensemanager.dto.request.UserCreateRequest;
import com.ubs.expensemanager.dto.response.ErrorResponse;
import com.ubs.expensemanager.dto.response.LoginResponse;
import com.ubs.expensemanager.dto.response.UserResponse;
import com.ubs.expensemanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * REST controller responsible for authentication endpoints.
 *
 * <p>Exposes operations for user login and related authentication flows.
 * All requests are validated and return structured responses, including errors
 * with detailed messages when input is invalid or credentials are incorrect.</p>
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthController {
    private final AuthService authService;

    /**
     * Authenticates a user using email and password.
     *
     * <p>Logs login attempts and returns a {@link LoginResponse} if authentication
     * is successful. Validation errors or invalid credentials are returned as
     * {@link ErrorResponse}.</p>
     *
     * @param request the login request containing email and password; must be valid
     * @return a {@link ResponseEntity} containing {@link LoginResponse} on success
     * @throws jakarta.validation.ValidationException if input validation fails
     */
    @Operation(
            summary = "User login",
            description = "Authenticates the user and returns authentication data"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login attempt for email={}", request.getEmail());
        LoginResponse response = authService.login(request);
        log.info("Login success for email={}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles user registration requests.
     *
     * <p>Receives a UserCreateRequest, validates it, and delegates to AuthService
     * to create a new user. Returns a LoginResponse containing the generated JWT
     * and user info. Check swag for further info. </p>
     *
     * @param request the user creation request payload
     * @return ResponseEntity with status 201 and LoginResponse body
     */
    @Operation(
            summary = "User registration",
            description = "Creates a new user and returns authentication data"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody UserCreateRequest request
    ) {
        log.info("Registration attempt for email={}", request.getEmail());
        LoginResponse response = authService.register(request);
        log.info("Registration success for email={}", request.getEmail());

        return ResponseEntity
                .created(URI.create("/api/users/" + response.getUser().getId()))
                .body(response);
    }
}

