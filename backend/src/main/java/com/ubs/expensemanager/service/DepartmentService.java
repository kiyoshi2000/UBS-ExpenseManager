package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.DepartmentCreateRequest;
import com.ubs.expensemanager.dto.request.DepartmentUpdateRequest;
import com.ubs.expensemanager.dto.response.DepartmentResponse;
import com.ubs.expensemanager.exception.ConflictException;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.mapper.DepartmentMapper;
import com.ubs.expensemanager.model.Currency;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.repository.CurrencyRepository;
import com.ubs.expensemanager.repository.DepartmentRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for handling business logic related to Departments.
 *
 * <p>This class orchestrates validation, persistence and transformation
 * between entities and DTOs. All department currency references are validated
 * against the currencies table to ensure referential integrity.</p>
 */
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CurrencyRepository currencyRepository;
    private final DepartmentMapper departmentMapper;

    /**
     * Creates a new department.
     *
     * @param request data required to create a department
     * @return created department as response DTO
     * @throws ResourceNotFoundException if the specified currency does not exist
     * @throws ConflictException if a department with the same name already exists
     * @throws IllegalArgumentException if daily budget exceeds monthly budget
     */
    public DepartmentResponse create(DepartmentCreateRequest request) {

        // Prevent duplicated department names
        if (departmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Department with this name already exists");
        }

        // Validate that the currency exists
        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with ID: " + request.getCurrencyId()));

        if (request.getDailyBudget() != null &&
            request.getDailyBudget().compareTo(request.getMonthlyBudget()) > 0) {
            throw new IllegalArgumentException(
                "Daily budget cannot be greater than monthly budget"
            );
        }

        Department department = Department.builder()
                .name(request.getName())
                .monthlyBudget(request.getMonthlyBudget())
                .dailyBudget(request.getDailyBudget())
                .currency(currency)
                .build();

        Department savedDepartment = departmentRepository.save(department);

        return departmentMapper.toResponse(savedDepartment);
    }

    /**
     * Retrieves all departments.
     *
     * @return list of departments
     */
    public List<DepartmentResponse> listAll() {
        return departmentRepository.findAll()
                .stream()
                .map(departmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing department.
     *
     * @param id      department identifier
     * @param request updated data
     * @return updated department
     * @throws ResourceNotFoundException if department or currency does not exist
     * @throws ConflictException if another department with the same name already exists
     * @throws IllegalArgumentException if daily budget exceeds monthly budget
     */
    public DepartmentResponse update(Long id, DepartmentUpdateRequest request) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        // If the name is being changed, ensure it does not conflict with another department
        if (!department.getName().equalsIgnoreCase(request.getName())
                && departmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Department with this name already exists");
        }

        // Validate that the currency exists
        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with ID: " + request.getCurrencyId()));

        if (request.getDailyBudget() != null &&
            request.getDailyBudget().compareTo(request.getMonthlyBudget()) > 0) {
            throw new IllegalArgumentException(
                "Daily budget cannot be greater than monthly budget"
            );
        }

        department.setName(request.getName());
        department.setMonthlyBudget(request.getMonthlyBudget());
        department.setDailyBudget(request.getDailyBudget()); 
        department.setCurrency(currency);

        Department updatedDepartment = departmentRepository.save(department);

        return departmentMapper.toResponse(updatedDepartment);
    }

    /**
     * Deletes a department by its identifier.
     *
     * @param id department identifier
     * @throws ResourceNotFoundException if department does not exist
     */
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found");
        }

        departmentRepository.deleteById(id);
    }

}
