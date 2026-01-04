package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.request.UserFilterRequest;
import com.ubs.expensemanager.dto.request.UserUpdateRequest;
import com.ubs.expensemanager.dto.response.ErrorResponse;
import com.ubs.expensemanager.dto.response.UserResponse;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<UserResponse>> findAll(
            @ModelAttribute UserFilterRequest filters,
            @PageableDefault(size = 10, sort = "name") @ParameterObject Pageable pageable
    ) {
        log.info("Retrieving Users with filters: {}, page:{}, size:{}", filters, pageable.getPageNumber(), pageable.getPageSize());
        Page<UserResponse> users = userService.findAll(filters, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves a specific User by their ID.
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
