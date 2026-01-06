package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.ExpenseCategoryCreateRequest;
import com.ubs.expensemanager.dto.request.ExpenseCategoryUpdateRequest;
import com.ubs.expensemanager.dto.response.ExpenseCategoryResponse;
import com.ubs.expensemanager.exception.ConflictException;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.repository.ExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for handling business logic related to Expense Categories.
 *
 * <p>This class orchestrates validation, persistence and transformation
 * between entities and DTOs.</p>
 */
@Service
@RequiredArgsConstructor
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository expenseCategoryRepository;

    /**
     * Creates a new expense category.
     *
     * @param request data required to create a category
     * @return created category as response DTO
     */
    public ExpenseCategoryResponse create(ExpenseCategoryCreateRequest request) {

        if (expenseCategoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Expense category with this name already exists");
        }

        ExpenseCategory category = ExpenseCategory.builder()
            .name(request.getName())
            .dailyBudget(request.getDailyBudget())
            .monthlyBudget(request.getMonthlyBudget())
            .build();

        ExpenseCategory savedCategory = expenseCategoryRepository.save(category);

        return toResponse(savedCategory);
    }

    /**
     * Retrieves all expense categories.
     *
     * @return list of categories
     */
    public List<ExpenseCategoryResponse> listAll() {
        return expenseCategoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single expense category by its identifier.
     *
     * @param id category identifier
     * @return category as response DTO
     */
    public ExpenseCategoryResponse findById(Long id) {
        ExpenseCategory category = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense category not found"));

        return toResponse(category);
    }

    /**
     * Updates an existing expense category.
     *
     * @param id      category identifier
     * @param request updated data
     * @return updated category
     */
    public ExpenseCategoryResponse update(Long id, ExpenseCategoryUpdateRequest request) {

        ExpenseCategory category = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense category not found"));

        if (!category.getName().equalsIgnoreCase(request.getName())
                && expenseCategoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Expense category with this name already exists");
        }

        category.setName(request.getName());
        category.setDailyBudget(request.getDailyBudget());
        category.setMonthlyBudget(request.getMonthlyBudget());

        ExpenseCategory updatedCategory = expenseCategoryRepository.save(category);

        return toResponse(updatedCategory);
    }

    /**
     * Deletes an expense category by its identifier.
     *
     * @param id category identifier
     */
    public void delete(Long id) {
        if (!expenseCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense category not found");
        }

        expenseCategoryRepository.deleteById(id);
    }

    /**
     * Maps an ExpenseCategory entity to an ExpenseCategoryResponse DTO.
     */
    private ExpenseCategoryResponse toResponse(ExpenseCategory category) {
        return ExpenseCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .dailyBudget(category.getDailyBudget())
                .monthlyBudget(category.getMonthlyBudget())
                .build();
    }
}
