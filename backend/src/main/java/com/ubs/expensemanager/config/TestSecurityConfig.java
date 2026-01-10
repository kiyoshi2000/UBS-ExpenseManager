package com.ubs.expensemanager.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test configuration for security-related beans. Uses NoOpPasswordEncoder and simple test user
 * details for simplicity in tests.
 */
@Configuration
@Profile("test")
@RequiredArgsConstructor
public class TestSecurityConfig {

  /**
   * Provides a NoOpPasswordEncoder for tests to avoid BCrypt complexity. This allows predictable
   * password handling in test scenarios.
   */
  @Bean
  @Primary
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  /**
   * Provides a simple actuator user details service for tests.
   */
  @Bean
  @Primary
  public UserDetailsService actuatorUserDetailsService() {
    return new InMemoryUserDetailsManager(
        User.withUsername("test-actuator")
            .password("test-password")
            .roles("ACTUATOR_ADMIN")
            .build()
    );
  }

  /**
   * Provides an AuthenticationManager for tests.
   */
  @Bean
  @Primary
  public AuthenticationManager authenticationManager(
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(authenticationProvider);
  }

  /**
   * Configures all rules to access the endpoints of application.
   *
   * @param http the http security
   * @throws Exception when it occurs and unexpected error
   */
  @Bean
  public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .anyRequest().permitAll()
        )
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }
}
