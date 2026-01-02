package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.request.DepartmentCreateRequest;
import com.ubs.expensemanager.dto.request.DepartmentUpdateRequest;
import com.ubs.expensemanager.dto.response.DepartmentResponse;
import com.ubs.expensemanager.dto.response.ErrorResponse;
import com.ubs.expensemanager.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller responsible for managing departments.
 *
 * <p>Only users with FINANCE role are allowed to create, update or delete
 * departments. Listing departments is allowed for authenticated users.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Department management endpoints")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * Creates a new department.
     */
    @Operation(
            summary = "Create a new department",
            description = "Creates a department with basic financial information"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Department created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Department already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<DepartmentResponse> create(
            @Valid @RequestBody DepartmentCreateRequest request) {

        log.info("Creating department with name={}", request.getName());

        DepartmentResponse response = departmentService.create(request);

        log.info("Department created successfully with id={}", response.getId());

        return ResponseEntity
                .created(URI.create("/api/departments/" + response.getId()))
                .body(response);
    }

    /**
     * Retrieves all registered departments.
     */
    @Operation(
            summary = "List all departments",
            description = "Returns all registered departments"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departments retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> listAll() {

        log.info("Listing all departments");

        return ResponseEntity.ok(departmentService.listAll());
    }

    /**
     * Updates an existing department.
     */
    @Operation(
            summary = "Update an existing department",
            description = "Updates department information by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Department name conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request) {

        log.info("Updating department id={}", id);

        DepartmentResponse response = departmentService.update(id, request);

        log.info("Department updated successfully id={}", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a department by its identifier.
     */
    @Operation(
            summary = "Delete a department",
            description = "Deletes a department by id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Department deleted successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found",
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

        log.info("Deleting department id={}", id);

        departmentService.delete(id);

        log.info("Department deleted successfully id={}", id);

        return ResponseEntity.noContent().build();
    }
}
