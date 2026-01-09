package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.DepartmentCreateRequest;
import com.ubs.expensemanager.dto.request.DepartmentUpdateRequest;
import com.ubs.expensemanager.dto.response.DepartmentResponse;
import com.ubs.expensemanager.exception.ConflictException;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for handling business logic related to Departments.
 *
 * <p>This class orchestrates validation, persistence and transformation
 * between entities and DTOs.</p>
 */
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    /**
     * Creates a new department.
     *
     * @param request data required to create a department
     * @return created department as response DTO
     */
    public DepartmentResponse create(DepartmentCreateRequest request) {

        // Prevent duplicated department names
        if (departmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Department with this name already exists");
        }

        Department department = Department.builder()
                .name(request.getName())
                .monthlyBudget(request.getMonthlyBudget())
                .dailyBudget(request.getDailyBudget())
                .currency(request.getCurrency())
                .build();

        Department savedDepartment = departmentRepository.save(department);

        return toResponse(savedDepartment);
    }

    /**
     * Retrieves all departments.
     *
     * @return list of departments
     */
    public List<DepartmentResponse> listAll() {
        return departmentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing department.
     *
     * @param id      department identifier
     * @param request updated data
     * @return updated department
     */
    public DepartmentResponse update(Long id, DepartmentUpdateRequest request) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        // If the name is being changed, ensure it does not conflict with another department
        if (!department.getName().equalsIgnoreCase(request.getName())
                && departmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Department with this name already exists");
        }

        department.setName(request.getName());
        department.setMonthlyBudget(request.getMonthlyBudget());
        department.setDailyBudget(request.getDailyBudget()); // ✅ FIX
        department.setCurrency(request.getCurrency());

        Department updatedDepartment = departmentRepository.save(department);

        return toResponse(updatedDepartment);
    }

    /**
     * Deletes a department by its identifier.
     *
     * @param id department identifier
     */
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found");
        }

        departmentRepository.deleteById(id);
    }

    /**
     * Maps a Department entity to a DepartmentResponse DTO.
     */
    private DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .monthlyBudget(department.getMonthlyBudget())
                .dailyBudget(department.getDailyBudget())
                .currency(department.getCurrency())
                .build();
    }
}
