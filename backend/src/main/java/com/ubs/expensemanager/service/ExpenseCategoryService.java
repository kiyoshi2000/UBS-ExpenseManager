package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.ExpenseCategoryCreateRequest;
import com.ubs.expensemanager.dto.request.ExpenseCategoryUpdateRequest;
import com.ubs.expensemanager.dto.response.ExpenseCategoryAuditResponse;
import com.ubs.expensemanager.dto.response.ExpenseCategoryResponse;
import com.ubs.expensemanager.exception.ConflictException;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.mapper.ExpenseCategoryMapper;
import com.ubs.expensemanager.model.Currency;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.repository.CurrencyRepository;
import com.ubs.expensemanager.repository.ExpenseCategoryRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;

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
    private final CurrencyRepository currencyRepository;
    private final EntityManager entityManager;
    private final ExpenseCategoryMapper expenseCategoryMapper;

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

        Currency currency = currencyRepository.findByName(request.getCurrencyName())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + request.getCurrencyName()));

        ExpenseCategory category = ExpenseCategory.builder()
            .name(request.getName())
            .dailyBudget(request.getDailyBudget())
            .monthlyBudget(request.getMonthlyBudget())
            .currency(currency)
            .build();

        ExpenseCategory savedCategory = expenseCategoryRepository.save(category);

        return expenseCategoryMapper.toResponse(savedCategory);
    }

    /**
     * Retrieves all expense categories.
     *
     * @return list of categories
     */
    public List<ExpenseCategoryResponse> listAll() {
        return expenseCategoryRepository.findAll()
                .stream()
                .map(expenseCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single expense category by its identifier.
     * 
     * <p> If dateTime is provided, queries the audit history to retrieve the version
     * that was active at the specified moment. Otherwise, retrieves the current version. </p>
     *
     * @param id       category identifier
     * @param dateTime optional date-time to query historical data (null for current version)
     * @return category as response DTO
     * @throws ResourceNotFoundException if category not found or no audit record exists for the date
     */
    public ExpenseCategoryResponse findById(Long id, OffsetDateTime dateTime) {

        // If no dateTime is provided, return the ExpenseCategory itself
        if (dateTime == null) {
            ExpenseCategory category = expenseCategoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Expense category not found"));
            return expenseCategoryMapper.toResponse(category);
        }
        // Else, find the audit relative to that provided date
        
        var auditReader = AuditReaderFactory.get(entityManager);
        
        Date queryDate = Date.from(dateTime.toInstant());
        
        List results = auditReader.createQuery()
                .forEntitiesAtRevision(ExpenseCategory.class, auditReader.getRevisions(ExpenseCategory.class, id)
                    .stream()
                    .filter(rev -> {
                        Date revDate = auditReader.getRevisionDate(rev);
                        return !revDate.after(queryDate);
                    })
                    .max((a, b) -> Integer.compare(a.intValue(), b.intValue()))
                    .orElseThrow(() -> new ResourceNotFoundException("No audit record found for category at specified date")))
                .add(AuditEntity.id().eq(id))
                .getResultList();
        
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Expense category not found in audit history");
        }
        
        return expenseCategoryMapper.toResponse((ExpenseCategory) results.getFirst());
    }

    /**
     * Retrieves the complete audit history for an expense category (asc order).
     *
     * @param id category identifier
     * @return list of all audited versions of the category
     */
    @SuppressWarnings("unchecked")
    public List<ExpenseCategoryAuditResponse> getAuditHistory(Long id) {
        if (!expenseCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense category not found");
        }

        var auditReader = AuditReaderFactory.get(entityManager);
        
        List<Object[]> results = auditReader.createQuery()
                .forRevisionsOfEntity(ExpenseCategory.class, false, true)
                .add(AuditEntity.id().eq(id))
                .getResultList();
        
        return results.stream()
                .map(result -> {
                    ExpenseCategory entity = (ExpenseCategory) result[0];
                    Number revNumber = (Number) ((org.hibernate.envers.DefaultRevisionEntity) result[1]).getId();
                    long revTimestamp = ((org.hibernate.envers.DefaultRevisionEntity) result[1]).getTimestamp();
                    RevisionType revType = (RevisionType) result[2];

                    return ExpenseCategoryAuditResponse.builder()
                            .id(entity.getId())
                            .name(entity.getName())
                            .dailyBudget(entity.getDailyBudget())
                            .monthlyBudget(entity.getMonthlyBudget())
                            .currencyName(entity.getCurrency() != null ? entity.getCurrency().getName() : null)
                            .exchangeRate(entity.getCurrency() != null ? entity.getCurrency().getExchangeRate() : null)
                            .revisionNumber(revNumber)
                            .revisionType((short) revType.ordinal())
                            .revisionDate(LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(revTimestamp), ZoneId.systemDefault()))
                            .build();
                })
                .collect(Collectors.toList());
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

        Currency currency = currencyRepository.findByName(request.getCurrencyName())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + request.getCurrencyName()));

        category.setName(request.getName());
        category.setDailyBudget(request.getDailyBudget());
        category.setMonthlyBudget(request.getMonthlyBudget());
        category.setCurrency(currency);

        ExpenseCategory updatedCategory = expenseCategoryRepository.save(category);

        return expenseCategoryMapper.toResponse(updatedCategory);
    }

}
