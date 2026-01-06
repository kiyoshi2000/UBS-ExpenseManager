package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.request.ExpenseCategoryCreateRequest;
import com.ubs.expensemanager.dto.request.ExpenseCategoryUpdateRequest;
import com.ubs.expensemanager.dto.response.ExpenseCategoryResponse;
import com.ubs.expensemanager.dto.response.ErrorResponse;
import com.ubs.expensemanager.service.ExpenseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller responsible for managing expense categories.
 *
 * <p>Only users with FINANCE role are allowed to create, update or delete
 * expense categories. Listing categories is allowed for authenticated users.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/expense-categories")
@RequiredArgsConstructor
@Tag(name = "Expense Categories", description = "Expense category management endpoints")
public class ExpenseCategoryController {

    private final ExpenseCategoryService expenseCategoryService;

    /**
     * Creates a new expense category.
     */
    @Operation(
            summary = "Create a new expense category",
            description = "Creates an expense category with budget limits"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Category already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<ExpenseCategoryResponse> create(
            @Valid @RequestBody ExpenseCategoryCreateRequest request) {

        log.info("Creating expense category with name={}", request.getName());

        ExpenseCategoryResponse response = expenseCategoryService.create(request);

        log.info("Expense category created successfully with id={}", response.getId());

        return ResponseEntity
                .created(URI.create("/api/expense-categories/" + response.getId()))
                .body(response);
    }

    /**
     * Retrieves all registered expense categories.
     */
    @Operation(
            summary = "List all expense categories",
            description = "Returns all registered expense categories"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<ExpenseCategoryResponse>> listAll() {

        log.info("Listing all expense categories");

        return ResponseEntity.ok(expenseCategoryService.listAll());
    }

    /**
     * Retrieves an expense category by its identifier.
     */
    @Operation(
            summary = "Get expense category by id",
            description = "Returns a single expense category by its identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseCategoryResponse> findById(@PathVariable Long id) {

        log.info("Retrieving expense category id={}", id);

        return ResponseEntity.ok(expenseCategoryService.findById(id));
    }

    /**
     * Updates an existing expense category.
     */
    @Operation(
            summary = "Update an existing expense category",
            description = "Updates expense category information by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Category name conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<ExpenseCategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseCategoryUpdateRequest request) {

        log.info("Updating expense category id={}", id);

        ExpenseCategoryResponse response = expenseCategoryService.update(id, request);

        log.info("Expense category updated successfully id={}", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an expense category by its identifier.
     */
    @Operation(
            summary = "Delete an expense category",
            description = "Deletes an expense category by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        log.info("Deleting expense category id={}", id);

        expenseCategoryService.delete(id);

        log.info("Expense category deleted successfully id={}", id);

        return ResponseEntity.noContent().build();
    }
}
