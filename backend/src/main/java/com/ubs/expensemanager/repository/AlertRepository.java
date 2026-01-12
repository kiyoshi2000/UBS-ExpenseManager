package com.ubs.expensemanager.repository;

import com.ubs.expensemanager.model.Alert;
import com.ubs.expensemanager.model.AlertStatus;
import com.ubs.expensemanager.model.AlertType;
import com.ubs.expensemanager.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository responsible for data access operations related to {@link Alert}.
 *
 * <p>This interface abstracts all persistence logic and provides
 * CRUD operations through Spring Data JPA, as well as custom methods
 * for finding alerts by type and status.</p>
 */
@Repository
public interface AlertRepository extends 
        JpaRepository<Alert, Long>,
        JpaSpecificationExecutor<Alert> {

    /**
     * Finds all alerts with the specified status.
     *
     * @param status the alert status
     * @return a list of alerts with the specified status
     */
    List<Alert> findByStatus(AlertStatus status);

    /**
     * Finds all alerts with the specified type.
     *
     * @param type the alert type
     * @return a list of alerts with the specified type
     */
    List<Alert> findByType(AlertType type);

    /**
     * Finds all alerts with the specified type and status.
     *
     * @param type the alert type
     * @param status the alert status
     * @return a list of alerts with the specified type and status
     */
    List<Alert> findByTypeAndStatus(AlertType type, AlertStatus status);

    /**
     * Finds an alert with the specified expense, type, and status.
     *
     * @param expense the expense associated with the alert
     * @param type the alert type
     * @param status the alert status
     * @return an optional containing the alert if found, or empty if not found
     */
    Optional<Alert> findByExpenseAndTypeAndStatus(Expense expense, AlertType type, AlertStatus status);

    /**
     * Finds all alerts with the specified expense, type, and status.
     *
     * @param expense the expense associated with the alerts
     * @param type the alert type
     * @param status the alert status
     * @return a list of alerts matching the criteria
     */
    List<Alert> findAllByExpenseAndTypeAndStatus(Expense expense, AlertType type, AlertStatus status);
}
