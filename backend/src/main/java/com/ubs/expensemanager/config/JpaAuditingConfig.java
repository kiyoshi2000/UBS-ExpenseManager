package com.ubs.expensemanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration for JPA Auditing.
 *
 * <p>Enables automatic population of @CreatedDate and @LastModifiedDate fields
 * in entities that extend the Auditable class.</p>
 *
 * <p>This configuration is disabled in test profile to avoid conflicts with
 * test database setup.</p>
 */
@Configuration
@EnableJpaAuditing
@Profile("!test")
public class JpaAuditingConfig {
}
