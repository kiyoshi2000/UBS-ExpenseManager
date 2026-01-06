package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.ExpenseCategoryCreateRequest;
import com.ubs.expensemanager.dto.request.ExpenseCategoryUpdateRequest;
import com.ubs.expensemanager.dto.response.ExpenseCategoryAuditResponse;
import com.ubs.expensemanager.dto.response.ExpenseCategoryResponse;
import com.ubs.expensemanager.exception.ConflictException;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.repository.ExpenseCategoryRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseCategoryServiceTest {

    @Mock
    ExpenseCategoryRepository expenseCategoryRepository;

    @Mock
    EntityManager entityManager;

    @Mock
    AuditReader auditReader;

    @Mock
    AuditQueryCreator auditQueryCreator;

    @Mock
    AuditQuery auditQuery;

    @InjectMocks
    ExpenseCategoryService expenseCategoryService;

    ExpenseCategory foodCategory;
    ExpenseCategory transportCategory;
    ExpenseCategory foodCategoryFirstVersion;
    ExpenseCategory foodCategorySecondVersion;

    @BeforeEach
    void setUp() {
        // Current version of food category (after 2 edits)
        foodCategory = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .dailyBudget(new BigDecimal("150.00"))
                .monthlyBudget(new BigDecimal("4500.00"))
                .build();

        // First version (initial creation)
        foodCategoryFirstVersion = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .dailyBudget(new BigDecimal("100.00"))
                .monthlyBudget(new BigDecimal("3000.00"))
                .build();

        // Second version (after first edit)
        foodCategorySecondVersion = ExpenseCategory.builder()
                .id(1L)
                .name("Food & Beverages")
                .dailyBudget(new BigDecimal("120.00"))
                .monthlyBudget(new BigDecimal("3600.00"))
                .build();

        transportCategory = ExpenseCategory.builder()
                .id(2L)
                .name("Transport")
                .dailyBudget(new BigDecimal("50.00"))
                .monthlyBudget(new BigDecimal("1500.00"))
                .build();
    }

    @Test
    void create_success() {
        ExpenseCategoryCreateRequest request = ExpenseCategoryCreateRequest.builder()
                .name("Food")
                .dailyBudget(new BigDecimal("100.00"))
                .monthlyBudget(new BigDecimal("3000.00"))
                .build();

        when(expenseCategoryRepository.existsByNameIgnoreCase("Food")).thenReturn(false);
        when(expenseCategoryRepository.save(any(ExpenseCategory.class))).thenAnswer(inv -> {
            ExpenseCategory saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ExpenseCategoryResponse response = expenseCategoryService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Food", response.getName());
        assertEquals(new BigDecimal("100.00"), response.getDailyBudget());
        assertEquals(new BigDecimal("3000.00"), response.getMonthlyBudget());

        verify(expenseCategoryRepository).existsByNameIgnoreCase("Food");
        verify(expenseCategoryRepository).save(any(ExpenseCategory.class));
    }

    @Test
    void listAll_returnsAllCategories() {
        List<ExpenseCategory> categories = List.of(foodCategory, transportCategory);
        when(expenseCategoryRepository.findAll()).thenReturn(categories);

        List<ExpenseCategoryResponse> result = expenseCategoryService.listAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Food", result.get(0).getName());
        assertEquals("Transport", result.get(1).getName());

        verify(expenseCategoryRepository).findAll();
    }

    @Test
    void findById_withoutDateTime_returnsCurrentVersion() {
        when(expenseCategoryRepository.findById(1L)).thenReturn(Optional.of(foodCategory));

        ExpenseCategoryResponse response = expenseCategoryService.findById(1L, null);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Food", response.getName());
        assertEquals(new BigDecimal("150.00"), response.getDailyBudget());
        assertEquals(new BigDecimal("4500.00"), response.getMonthlyBudget());

        verify(expenseCategoryRepository).findById(1L);
        verifyNoInteractions(entityManager);
    }

    @Test
    void findById_withDateTime_returnsHistoricalVersion() {
        // Date that corresponds to the first version (January 1, 2026)
        OffsetDateTime queryDateTime = OffsetDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        Date queryDate = Date.from(queryDateTime.toInstant());

        // Revision 1: January 1, 2026 10:00 AM (creation)
        Number revision1 = 1;
        Date revision1Date = Date.from(OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC).toInstant());

        try (MockedStatic<AuditReaderFactory> mockedFactory = mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            when(auditReader.getRevisions(ExpenseCategory.class, 1L))
                    .thenReturn(List.of(revision1));
            when(auditReader.getRevisionDate(revision1)).thenReturn(revision1Date);
            when(auditReader.createQuery()).thenReturn(auditQueryCreator);
            when(auditQueryCreator.forEntitiesAtRevision(eq(ExpenseCategory.class), eq(revision1)))
                    .thenReturn(auditQuery);
            when(auditQuery.add(any(AuditCriterion.class))).thenReturn(auditQuery);
            when(auditQuery.getResultList()).thenReturn(List.of(foodCategoryFirstVersion));

            ExpenseCategoryResponse response = expenseCategoryService.findById(1L, queryDateTime);

            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Food", response.getName());
            assertEquals(new BigDecimal("100.00"), response.getDailyBudget());
            assertEquals(new BigDecimal("3000.00"), response.getMonthlyBudget());
        }
    }

    @Test
    void findById_withDateTime_noAuditRecord_throwsException() {
        OffsetDateTime queryDateTime = OffsetDateTime.of(2025, 12, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        try (MockedStatic<AuditReaderFactory> mockedFactory = mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            when(auditReader.getRevisions(ExpenseCategory.class, 1L))
                    .thenReturn(List.of());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> expenseCategoryService.findById(1L, queryDateTime)
            );
        }
    }

    @Test
    void findById_withDateTime_emptyResultList_throwsException() {
        OffsetDateTime queryDateTime = OffsetDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        Number revision1 = 1;
        Date revision1Date = Date.from(OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC).toInstant());

        try (MockedStatic<AuditReaderFactory> mockedFactory = mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            when(auditReader.getRevisions(ExpenseCategory.class, 1L))
                    .thenReturn(List.of(revision1));
            when(auditReader.getRevisionDate(revision1)).thenReturn(revision1Date);
            when(auditReader.createQuery()).thenReturn(auditQueryCreator);
            when(auditQueryCreator.forEntitiesAtRevision(eq(ExpenseCategory.class), eq(revision1)))
                    .thenReturn(auditQuery);
            when(auditQuery.add(any(AuditCriterion.class))).thenReturn(auditQuery);
            when(auditQuery.getResultList()).thenReturn(List.of());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> expenseCategoryService.findById(1L, queryDateTime)
            );
        }
    }

    @Test
    void getAuditHistory_success_withMultipleRevisions() {
        // Revision 1: January 1, 2026 10:00 AM (creation - INSERT)
        DefaultRevisionEntity rev1 = new DefaultRevisionEntity();
        rev1.setId(1);
        rev1.setTimestamp(OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC)
                .toInstant().toEpochMilli());

        // Revision 2: January 2, 2026 02:00 PM (first update - MOD)
        DefaultRevisionEntity rev2 = new DefaultRevisionEntity();
        rev2.setId(2);
        rev2.setTimestamp(OffsetDateTime.of(2026, 1, 2, 14, 0, 0, 0, ZoneOffset.UTC)
                .toInstant().toEpochMilli());

        // Revision 3: January 3, 2026 04:00 PM (second update - MOD)
        DefaultRevisionEntity rev3 = new DefaultRevisionEntity();
        rev3.setId(3);
        rev3.setTimestamp(OffsetDateTime.of(2026, 1, 3, 16, 0, 0, 0, ZoneOffset.UTC)
                .toInstant().toEpochMilli());

        List<Object[]> auditResults = List.of(
                new Object[]{foodCategoryFirstVersion, rev1, RevisionType.ADD},
                new Object[]{foodCategorySecondVersion, rev2, RevisionType.MOD},
                new Object[]{foodCategory, rev3, RevisionType.MOD}
        );

        try (MockedStatic<AuditReaderFactory> mockedFactory = mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            when(expenseCategoryRepository.existsById(1L)).thenReturn(true);
            when(auditReader.createQuery()).thenReturn(auditQueryCreator);
            when(auditQueryCreator.forRevisionsOfEntity(ExpenseCategory.class, false, true))
                    .thenReturn(auditQuery);
            when(auditQuery.add(any(AuditCriterion.class))).thenReturn(auditQuery);
            when(auditQuery.getResultList()).thenReturn(auditResults);

            List<ExpenseCategoryAuditResponse> history = expenseCategoryService.getAuditHistory(1L);

            assertNotNull(history);
            assertEquals(3, history.size());

            // Verify first version (creation)
            ExpenseCategoryAuditResponse firstAudit = history.getFirst();
            assertEquals(1L, firstAudit.getId());
            assertEquals("Food", firstAudit.getName());
            assertEquals(new BigDecimal("100.00"), firstAudit.getDailyBudget());
            assertEquals(new BigDecimal("3000.00"), firstAudit.getMonthlyBudget());
            assertEquals(1, firstAudit.getRevisionNumber().intValue());
            assertEquals((short) RevisionType.ADD.ordinal(), firstAudit.getRevisionType());

            // Verify second version (first edit)
            ExpenseCategoryAuditResponse secondAudit = history.get(1);
            assertEquals(1L, secondAudit.getId());
            assertEquals("Food & Beverages", secondAudit.getName());
            assertEquals(new BigDecimal("120.00"), secondAudit.getDailyBudget());
            assertEquals(new BigDecimal("3600.00"), secondAudit.getMonthlyBudget());
            assertEquals(2, secondAudit.getRevisionNumber().intValue());
            assertEquals((short) RevisionType.MOD.ordinal(), secondAudit.getRevisionType());

            // Verify third version (second edit)
            ExpenseCategoryAuditResponse thirdAudit = history.get(2);
            assertEquals(1L, thirdAudit.getId());
            assertEquals("Food", thirdAudit.getName());
            assertEquals(new BigDecimal("150.00"), thirdAudit.getDailyBudget());
            assertEquals(new BigDecimal("4500.00"), thirdAudit.getMonthlyBudget());
            assertEquals(3, thirdAudit.getRevisionNumber().intValue());
            assertEquals((short) RevisionType.MOD.ordinal(), thirdAudit.getRevisionType());
        }
    }

    @Test
    void getAuditHistory_categoryNotFound_throwsException() {
        when(expenseCategoryRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> expenseCategoryService.getAuditHistory(99L)
        );

        verify(expenseCategoryRepository).existsById(99L);
    }

    @Test
    void update_success() {
        ExpenseCategoryUpdateRequest request = ExpenseCategoryUpdateRequest.builder()
                .name("Food Updated")
                .dailyBudget(new BigDecimal("200.00"))
                .monthlyBudget(new BigDecimal("6000.00"))
                .build();

        when(expenseCategoryRepository.findById(1L)).thenReturn(Optional.of(foodCategory));
        when(expenseCategoryRepository.existsByNameIgnoreCase("Food Updated")).thenReturn(false);
        when(expenseCategoryRepository.save(any(ExpenseCategory.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseCategoryResponse response = expenseCategoryService.update(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Food Updated", response.getName());
        assertEquals(new BigDecimal("200.00"), response.getDailyBudget());
        assertEquals(new BigDecimal("6000.00"), response.getMonthlyBudget());

        verify(expenseCategoryRepository).findById(1L);
        verify(expenseCategoryRepository).existsByNameIgnoreCase("Food Updated");
        verify(expenseCategoryRepository).save(any(ExpenseCategory.class));
    }
}
