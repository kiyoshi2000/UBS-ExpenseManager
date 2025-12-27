package com.ubs.expensemanager.repository;

import com.ubs.expensemanager.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository responsible for data access operations related to {@link Department}.
 *
 * This interface abstracts all persistence logic and provides
 * CRUD operations through Spring Data JPA.
 */

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}