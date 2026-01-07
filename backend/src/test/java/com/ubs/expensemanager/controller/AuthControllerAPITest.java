package com.ubs.expensemanager.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.database.rider.core.api.dataset.DataSet;
import com.ubs.expensemanager.dto.request.LoginRequest;
import com.ubs.expensemanager.dto.response.LoginResponse;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Integration test for {@link AuthController}
 */
public class AuthControllerAPITest extends ControllerAPITest {

  private static final String BASE_DATASET = "datasets/auth/";

  @MockBean
  private AuthenticationManager authenticationManager;

  @MockBean
  private JwtUtil jwtUtil;

  @BeforeEach
  void init() {
    basePath = "http://localhost:%d/api/auth";

    // Default: any login attempt fails -> 401
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Invalid credentials"));
  }

  /**
   * Verifies if {@link AuthController#login(LoginRequest, HttpServletResponse)} will successfully
   * authenticate a user when the request is valid.
   */
  @Test
  @DataSet(BASE_DATASET + "input/empty.yml")
  void shouldLoginInWhenRequestIsValid() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/auth/request/login.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/login";

    User user = User.builder()
        .id(1L)
        .name("Ubs User")
        .email("john@email.com")
        .role(UserRole.EMPLOYEE)
        .build();

    Authentication authentication = Mockito.mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(user);

    // Override default failure ONLY for this test: login succeeds
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);

    when(jwtUtil.generateToken(any(User.class))).thenReturn("fake-jwt-token");

    // when
    ResponseEntity<LoginResponse> response = restTemplate.exchange(
        requestPath,
        HttpMethod.POST,
        new HttpEntity<>(data, headers),
        LoginResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals("fake-jwt-token", Objects.requireNonNull(response.getBody()).getToken()),
        () -> assertNotNull(response.getBody().getUser()),
        () -> assertEquals("Ubs User", response.getBody().getUser().getName()),
        () -> assertEquals(1L, response.getBody().getUser().getId()),
        () -> assertNotNull(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)),
        () -> assertTrue(
            Objects.requireNonNull(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("jwt_token="))
    );
  }

  /**
   * Verifies if {@link AuthController#login(LoginRequest, HttpServletResponse)} will return 401
   * when password is incorrect.
   */
  @Test
  @DataSet(BASE_DATASET + "input/empty.yml")
  void shouldReturn401WhenPasswordIsIncorrect() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/auth/request/login-wrong-password.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/login";

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        requestPath,
        HttpMethod.POST,
        new HttpEntity<>(data, headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link AuthController#login(LoginRequest, HttpServletResponse)} will return 401
   * when email does not exist.
   */
  @Test
  @DataSet(BASE_DATASET + "input/empty.yml")
  void shouldReturn401WhenEmailDoesNotExist() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/auth/request/login-wrong-email.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/login";

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        requestPath,
        HttpMethod.POST,
        new HttpEntity<>(data, headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link AuthController#login(LoginRequest, HttpServletResponse)} will return 400
   * when email is empty.
   */
  @Test
  @DataSet(BASE_DATASET + "input/empty.yml")
  void shouldReturn400WhenEmailIsEmpty() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/auth/request/login-empty-email.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/login";

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        requestPath,
        HttpMethod.POST,
        new HttpEntity<>(data, headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link AuthController#login(LoginRequest, HttpServletResponse)} will return 400
   * when email format is invalid.
   */
  @Test
  @DataSet(BASE_DATASET + "input/empty.yml")
  void shouldReturn400WhenEmailFormatIsInvalid() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/auth/request/login-invalid-email.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/login";

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        requestPath,
        HttpMethod.POST,
        new HttpEntity<>(data, headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link AuthController#login(LoginRequest, HttpServletResponse)} will return 400
   * when password is empty.
   */
  @Test
  @DataSet(BASE_DATASET + "input/empty.yml")
  void shouldReturn400WhenPasswordIsEmpty() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/auth/request/login-empty-password.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/login";

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        requestPath,
        HttpMethod.POST,
        new HttpEntity<>(data, headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
    );
  }
}
