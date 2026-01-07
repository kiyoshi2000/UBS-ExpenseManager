package com.ubs.expensemanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

/**
 * Represents a Currency within the Expense Manager domain.
 *
 * <p> This entity is mapped to the "currencies" table and stores
 * currency information with exchange rates relative to a base currency (USD). </p>
 */
@Entity
@Table(
    name = "currencies",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_currencies_name", columnNames = "name")
    }
)
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Currency code (ISO 4217 format).
     * Must be exactly 3 characters (e.g., BRL, USD, EUR).
     */
    @Column(nullable = false, length = 3, unique = true)
    private String name;

    /**
     * Exchange rate relative to USD.
     * For USD itself, this should be 1.0.
     * For other currencies, represents how many units equal 1 USD.
     * Example: BRL with exchangeRate of 5.0 means 1 USD = 5 BRL
     */
    @Column(name = "exchange_rate", nullable = false, precision = 15, scale = 6)
    private BigDecimal exchangeRate;
}
