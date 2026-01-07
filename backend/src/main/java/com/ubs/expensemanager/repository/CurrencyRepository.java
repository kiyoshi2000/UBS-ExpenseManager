package com.ubs.expensemanager.repository;

import com.ubs.expensemanager.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository responsible for data access operations related to {@link Currency}.
 *
 * This interface abstracts all persistence logic and provides
 * CRUD operations through Spring Data JPA.
 */
@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByName(String name);
    boolean existsByName(String name);
}
