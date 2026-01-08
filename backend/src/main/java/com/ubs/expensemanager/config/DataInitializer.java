package com.ubs.expensemanager.config;

import com.ubs.expensemanager.model.Currency;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.CurrencyRepository;
import com.ubs.expensemanager.repository.DepartmentRepository;
import com.ubs.expensemanager.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Initializes basic application data on Spring Boot startup.
 *
 * <p> Creates predefined users with passwords encoded using the configured PasswordEncoder.
 * This is needed because bcrypt uses a random salt, so passwords cannot be reliably
 * inserted directly via SQL. </p>
 */
@Component
@Profile("!test")
public class DataInitializer {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrencyRepository currencyRepository;

    public DataInitializer(UserRepository userRepository, 
                          DepartmentRepository departmentRepository,
                          PasswordEncoder passwordEncoder,
                          CurrencyRepository currencyRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.currencyRepository = currencyRepository;
    }

    /**
     * Method executed after Spring Boot initialization.
     * Creates predefined users if they do not already exist in the database.
     */
    @PostConstruct
    public void init() {
        // Initialize currencies first (will create audit records automatically via Envers)
        initializeCurrencies();
        
        // Create IT department if it doesn't exist
        Department itDepartment = departmentRepository.findByNameIgnoreCase("IT")
                .orElseGet(() -> departmentRepository.save(
                        Department.builder()
                                .name("IT")
                                .monthlyBudget(new BigDecimal("50000.00"))
                                .dailyBudget(new BigDecimal("2000.00"))
                                .currency("USD")
                                .build()
                ));

        List<User> usersWithoutManager = List.of(
                User.builder()
                        .email("finance@ubs.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.FINANCE)
                        .name("Finance User")
                        .department(itDepartment)
                        .build(),
                User.builder()
                        .email("manager@ubs.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.MANAGER)
                        .name("Manager User")
                        .department(itDepartment)
                        .build()
        );

        // Save each user if it does not already exist
        for (User user : usersWithoutManager) {
            userRepository.findByEmail(user.getEmail())
                    .orElseGet(() -> userRepository.save(user));
        }

        User manager = userRepository.findByEmail("manager@ubs.com")
                .orElseThrow(() -> new IllegalStateException("Manager should have been created"));

        List<User> employees = List.of(
                User.builder()
                        .name("Employee One")
                        .email("employee@ubs.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.EMPLOYEE)
                        .manager(manager)
                        .department(itDepartment)
                        .build(),
                User.builder()
                        .name("Employee Two")
                        .email("employee2@ubs.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.EMPLOYEE)
                        .manager(manager)
                        .department(itDepartment)
                        .build()
        );

        for (User employee : employees) {
            userRepository.findByEmail(employee.getEmail())
                    .orElseGet(() -> userRepository.save(employee));
        }
    }

    /**
     * Initializes base currencies in the system.
     * USD is the base currency with exchange rate 1.0.
     * BRL is initialized with an approximate exchange rate.
     * 
     * Saving through repository ensures Envers creates audit records automatically.
     */
    private void initializeCurrencies() {
        // Initialize USD (base currency)
        currencyRepository.findByName("USD")
                .orElseGet(() -> currencyRepository.save(
                        Currency.builder()
                                .name("USD")
                                .exchangeRate(new BigDecimal("1.000000"))
                                .build()
                ));

        // Initialize BRL with approximate exchange rate
        // This value should be updated regularly via external API
        currencyRepository.findByName("BRL")
                .orElseGet(() -> currencyRepository.save(
                        Currency.builder()
                                .name("BRL")
                                .exchangeRate(new BigDecimal("5.500000"))
                                .build()
                ));
    }
}
