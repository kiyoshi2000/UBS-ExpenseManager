package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.request.LoginRequest;
import com.ubs.expensemanager.dto.response.ErrorResponse;
import com.ubs.expensemanager.dto.response.LoginResponse;
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

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "User Login",
            description = "Authenticate the user with email and password"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                    {
                                      "timestamp": "2025-12-20T15:05:29.519982678Z",
                                      "status": 400,
                                      "error": "Validation error",
                                      "message": "Validation failed for one or more fields",
                                      "path": "/api/auth/login",
                                      "errors": {
                                        "email": "must be a well-formed email address"
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Invalid Credentials",
                                    value = """
                                    {
                                      "timestamp": "2025-12-20T15:04:06.098997078Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Invalid Credentials",
                                      "path": "/api/auth/login"
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login attempt for email={}", request.getEmail());
        LoginResponse res = authService.authenticate(request);
        log.info("Login success for email={}", request.getEmail());

        return ResponseEntity.ok(res);
    }
}

