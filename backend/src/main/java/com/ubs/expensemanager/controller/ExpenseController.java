package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.request.ExpenseCreateRequest;
import com.ubs.expensemanager.dto.request.ExpenseFilterRequest;
import com.ubs.expensemanager.dto.request.ExpenseUpdateRequest;
import com.ubs.expensemanager.dto.response.ErrorResponse;
import com.ubs.expensemanager.dto.response.ExpenseResponse;
import com.ubs.expensemanager.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST controller responsible for expense management endpoints.
 *
 * <p>Exposes operations for creating, retrieving, updating, and managing expenses
 * including approval workflow operations. All requests require proper authentication and return
 * structured responses.</p>
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Expenses", description = "Expense Management Endpoints")
public class ExpenseController {

  private final ExpenseService expenseService;

  @Operation(
      summary = "Create new Expense",
      description = "Creates a new expense for the authenticated user. " +
          "The user is automatically extracted from the JWT token. " +
          "Budget validation is performed (warnings only)."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "Expense created successfully",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ExpenseResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid request data",
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
          description = "Expense category or currency not found",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      )
  })
  @PostMapping
  public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest request) {
    log.info("Creating new expense: category={}, amount={}", request.getExpenseCategoryId(),
        request.getAmount());
    ExpenseResponse response = expenseService.create(request);
    return ResponseEntity.created(URI.create("/api/expenses/" + response.getId())).body(response);
  }

  @Operation(
      summary = "List all Expenses",
      description = "Retrieves expenses with optional filtering and pagination. " +
          "EMPLOYEE users only see their own expenses. " +
          "MANAGER and FINANCE users can see all expenses and filter by user."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Expenses retrieved successfully",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = Page.class)
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
  public ResponseEntity<Page<ExpenseResponse>> findAll(
      @ModelAttribute ExpenseFilterRequest filters,
      @PageableDefault(size = 10, sort = "expenseDate") @ParameterObject Pageable pageable
  ) {
    log.info("Retrieving expenses with filters: {}, page: {}, size: {}", filters,
        pageable.getPageNumber(), pageable.getPageSize());
    Page<ExpenseResponse> expenses = expenseService.findAll(filters, pageable);
    return ResponseEntity.ok(expenses);
  }

  @Operation(
      summary = "Get Expense by ID",
      description = "Retrieves detailed information about a specific expense. " +
          "EMPLOYEE users can only view their own expenses."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Expense found",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ExpenseResponse.class)
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
          responseCode = "403",
          description = "Forbidden - Not authorized to access this expense",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Expense not found",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      )
  })
  @GetMapping("/{id}")
  public ResponseEntity<ExpenseResponse> findById(@PathVariable Long id) {
    log.info("Retrieving expense with id={}", id);
    ExpenseResponse expense = expenseService.findById(id);
    return ResponseEntity.ok(expense);
  }

  @Operation(
      summary = "Update Expense",
      description = "Updates an existing expense. " +
          "Only the expense owner can update. " +
          "Only expenses with status PENDING or REQUIRES_REVISION can be updated. " +
          "Status is reset to PENDING if it was REQUIRES_REVISION."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Expense updated successfully",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ExpenseResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid request data or invalid status for update",
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
          responseCode = "403",
          description = "Forbidden - Not authorized to modify this expense",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Expense, category, or currency not found",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      )
  })
  @PutMapping("/{id}")
  public ResponseEntity<ExpenseResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody ExpenseUpdateRequest request
  ) {
    log.info("Updating expense with id={}", id);
    ExpenseResponse response = expenseService.update(id, request);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Approve Expense",
      description = "Approves an expense based on user role. " +
          "MANAGER: PENDING → APPROVED_BY_MANAGER. " +
          "FINANCE: APPROVED_BY_MANAGER → APPROVED_BY_FINANCE."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Expense approved successfully",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ExpenseResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid status transition",
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
          responseCode = "403",
          description = "Forbidden - Insufficient permissions",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Expense not found",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      )
  })
  @PatchMapping("/{id}/approve")
  @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE')")
  public ResponseEntity<ExpenseResponse> approve(@PathVariable Long id) {
    log.info("Approving expense with id={}", id);
    ExpenseResponse response = expenseService.approve(id);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Reject Expense",
      description = "Rejects an expense based on user role. " +
          "MANAGER: PENDING or REQUIRES_REVISION → REJECTED. " +
          "FINANCE: APPROVED_BY_MANAGER or REQUIRES_REVISION → REJECTED."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Expense rejected successfully",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ExpenseResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid status transition",
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
          responseCode = "403",
          description = "Forbidden - Insufficient permissions",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Expense not found",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      )
  })
  @PatchMapping("/{id}/reject")
  @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE')")
  public ResponseEntity<ExpenseResponse> reject(@PathVariable Long id) {
    log.info("Rejecting expense with id={}", id);
    ExpenseResponse response = expenseService.reject(id);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Request Expense Revision",
      description = "Requests revision for an expense. " +
          "Can be used for any status except REJECTED or APPROVED_BY_FINANCE. " +
          "Changes status to REQUIRES_REVISION."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Revision requested successfully",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ExpenseResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid status transition",
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
          responseCode = "403",
          description = "Forbidden - Insufficient permissions",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Expense not found",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      )
  })
  @PatchMapping("/{id}/request-revision")
  @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE')")
  public ResponseEntity<ExpenseResponse> requestRevision(@PathVariable Long id) {
    log.info("Requesting revision for expense with id={}", id);
    ExpenseResponse response = expenseService.requestRevision(id);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Delete Expense",
      description = "Deletes an expense. " +
          "Only the expense owner can delete. " +
          "Only expenses with status PENDING can be deleted."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "Expense deleted successfully"
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid status for deletion",
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
          responseCode = "403",
          description = "Forbidden - Not authorized to delete this expense",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Expense not found",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)
          )
      )
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    log.info("Deleting expense with id={}", id);
    expenseService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
