package com.ubs.expensemanager.model;

import com.ubs.expensemanager.model.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "expenses")
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense extends Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Amount of the expense in the specified currency.
   */
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  /**
   * Optional description of the expense.
   */
  @Column(length = 500)
  private String description;

  /**
   * Date when the expense occurred.
   */
  @Column(name = "expense_date", nullable = false)
  private LocalDate expenseDate;

  /**
   * User who created the expense.
   */
  @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * Expense category for budget tracking.
   */
  @ManyToOne
  @JoinColumn(name = "expense_category_id", nullable = false)
  private ExpenseCategory expenseCategory;

  /**
   * Currency in which the expense was incurred.
   */
  @ManyToOne
  @JoinColumn(name = "currency_id", nullable = false)
  private Currency currency;

  /**
   * Optional URL to the expense receipt/proof.
   */
  @Column(name = "receipt_url", length = 1000)
  private String receiptUrl;

  /**
   * Current status of the expense in the approval workflow. Defaults to PENDING when a new expense
   * is created.
   */
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false, length = 50)
  private ExpenseStatus status = ExpenseStatus.PENDING;

}
