package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.request.UserFilterRequest;
import com.ubs.expensemanager.dto.request.UserUpdateRequest;
import com.ubs.expensemanager.dto.response.ErrorResponse;
import com.ubs.expensemanager.dto.response.UserResponse;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for user management endpoints.
 *
 * <p>Exposes operations for retrieving and updating user information.
 * For user creation and authentication, see {@link AuthController}.
 * All requests require proper authentication and return structured responses.</p>
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Users Management Endpoints")
public class UserController {
    private final UserService userService;

    /**
     * Retrieves all users with optional filters.
     *
     * <p> Supports filtering by:
     * <ul>
     *   <li>{@code role} - filter by user role</li>
     *   <li>{@code includeInactive} - include inactive users (default: false)</li>
     * </ul>
     * </p>
     *
     * @param filters the filter criteria for users
     * @return ResponseEntity containing list of users matching the filters
     * @see UserFilterRequest
     */
    @Operation(
            summary = "List all Users",
            description = "Retrieves all registered Users in the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
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
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll(
            @ModelAttribute UserFilterRequest filters
    ) {
        log.info("Retrieving Users with filters: {}", filters);
        List<UserResponse> users = userService.findAll(filters);
        log.info("Retrieved {} Users", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves a specific User by their ID.
     *
     * <p>Returns detailed information about a single user.
     * Requires authentication to access this endpoint.</p>
     *
     * @param id the unique identifier of the user
     * @return a {@link ResponseEntity} containing the {@link UserResponse}
     */
    @Operation(
            summary = "Get User by ID",
            description = "Retrieves detailed information about a specific user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        log.info("Retrieving User with id={}", id);
        UserResponse user = userService.findById(id);
        log.info("User found: email={}", user.getEmail());
        return ResponseEntity.ok(user);
    }

    /**
     * Updates an existing user's information.
     *
     * <p>Allows partial updates of user data including email, password, role,
     * and manager assignment. All fields in the request are optional.
     * Validates business rules such as manager requirements for users.
     * For creating new users, see {@link AuthController#register}.</p>
     *
     * @param id the unique identifier of the user to update
     * @param request the update request containing the fields to modify
     * @return a {@link ResponseEntity} containing the updated {@link UserResponse}
     */
    @Operation(
            summary = "Update User",
            description = "Updates an existing user's information. All fields are optional. " +
                    "EMPLOYEE role requires a manager. MANAGER cannot have a manager."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or business rule violation",
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or manager not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already in use",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("Update attempt for user with id={}", id);
        UserResponse updatedUser = userService.update(id, request);
        log.info("User updated successfully: email={}", updatedUser.getEmail());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes an existing user.
     *
     * @param id the unique identifier of the user to soft-delete
     * @return a {@link ResponseEntity} with no content
     */
    @Operation(
            summary = "Deactivate User",
            description = "Deactivate an existing user by id."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User deactivated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deactivate attempt for user with id={}", id);
        userService.deactivate(id);
        log.info("User deactivated successfully: id={}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deactivated user.
     *
     * <p>Only deactivated users can be reactivated. If the user is a manager,
     * their manager relationship must still be valid.</p>
     *
     * @param id the unique identifier of the user to reactivate
     * @return a {@link ResponseEntity} containing the reactivated {@link UserResponse}
     */
    @Operation(
            summary = "Reactivate User",
            description = "Reactivates a previously deactivated user"
    )
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<UserResponse> reactivate(@PathVariable Long id) {
        log.info("Reactivation attempt for user with id={}", id);
        UserResponse user = userService.reactivate(id);
        log.info("User reactivated successfully: email={}", user.getEmail());
        return ResponseEntity.ok(user);
    }
}
