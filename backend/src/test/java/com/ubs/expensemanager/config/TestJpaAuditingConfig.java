package com.ubs.expensemanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Test configuration for JPA Auditing.
 *
 * <p>Enables automatic population of @CreatedDate and @LastModifiedDate fields
 * in test environment.</p>
 */
@TestConfiguration
@EnableJpaAuditing
@Profile("test")
public class TestJpaAuditingConfig {
}
