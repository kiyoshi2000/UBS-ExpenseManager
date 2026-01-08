package com.ubs.expensemanager.repository;

import com.ubs.expensemanager.model.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository responsible for data access operations related to {@link ExpenseCategory}.
 *
 * This interface abstracts all persistence logic and provides
 * CRUD operations through Spring Data JPA.
 */
@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    Optional<ExpenseCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
