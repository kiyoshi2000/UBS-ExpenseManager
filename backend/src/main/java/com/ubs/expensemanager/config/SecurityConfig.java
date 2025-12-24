package com.ubs.expensemanager.config;

import com.ubs.expensemanager.security.JwtAuthFilter;
import com.ubs.expensemanager.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Central security configuration for the application.
 *
 * <p>Defines authentication requirements and cross-origin rules
 * applied to all HTTP requests.</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private static final String[] PUBLIC_ENDPOINTS = {
      "/api/auth/login",
      "/api/auth/register",

      "/swagger-ui/**",
      "/v3/api-docs/**",
      "/configuration/ui",
      "/swagger-resources/**",
      "/configuration/security",
      "/webjars/**",
      "/sessions/**"
  };

  private static final String[] ACTUATOR_ENDPOINTS = {"/actuator/**"};

  @Value("${spring.security.actuator-user.name}")
  private String actuatorUsername;

  @Value("${spring.security.actuator-user.password}")
  private String actuatorPassword;

  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  private final UserDetailsServiceImpl userDetailsService;
  private final JwtAuthFilter jwtAuthFilter;

  public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtAuthFilter jwtAuthFilter) {
    this.userDetailsService = userDetailsService;
    this.jwtAuthFilter = jwtAuthFilter;
  }

  /**
   * Applies security rules to all HTTP requests.
   *
   * <ul>
   *     <li>CSRF is disabled because the API does not rely on server-side sessions</li>
   *     <li>CORS is enabled to allow browser-based frontends to access the API</li>
   *     <li>Session management is stateless to enforce token-based authentication</li>
   *     <li>Authorization rules restrict access to authenticated users by default</li>
   * </ul>
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher("/**")
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  /**
   * Security filter chain for actuator endpoints with Basic Authentication.
   * <p>
   * Provides monitoring and health check endpoints protected by basic authentication using
   * credentials from application properties.
   *
   * @param http the {@link HttpSecurity} object used for configuring security settings
   * @return the configured {@link SecurityFilterChain}
   * @throws Exception if an error occurs while building the security filter chain
   */
  @Bean
  @Order(1)
  public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher(ACTUATOR_ENDPOINTS)
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(ACTUATOR_ENDPOINTS).authenticated())
        .httpBasic(Customizer.withDefaults())
        .userDetailsService(actuatorUserDetailsService())
        .build();
  }

  /**
   * Enables CORS for local frontend applications during development.
   *
   * <p> Get from ambient variable
   * {@code CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:5174}</p>
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
      }
    };
  }

  /**
   * Provides the authentication logic using the application's UserDetailsService and the configured
   * PasswordEncoder.
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
   * Exposes the AuthenticationManager from Spring's configuration for manual use.
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * In-memory user details service for actuator endpoints authentication.
   * <p>
   * Creates a single admin user for accessing monitoring and health endpoints. Credentials are
   * configured via application properties.
   *
   * @return configured {@link UserDetailsService} with admin user
   */
  @Bean
  public UserDetailsService actuatorUserDetailsService() {
    UserDetails actuatorUser = User.withUsername(actuatorUsername)
        .password(passwordEncoder().encode(actuatorPassword))
        .roles("ACTUATOR_ADMIN")
        .build();

    return new InMemoryUserDetailsManager(actuatorUser);
  }
}
