package com.ubs.expensemanager.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.ubs.expensemanager.dto.request.LoginRequest;
import com.ubs.expensemanager.dto.request.UserCreateRequest;
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
  @DataSet(BASE_DATASET + "input/employee-user-registered.yml")
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
        .email("employee@ubs.com")
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
        () -> {
          assertNotNull(response.getBody());
          assertNotNull(response.getBody().getUser());
          assertEquals("Ubs User", response.getBody().getUser().getName());
          assertEquals(1L, response.getBody().getUser().getId());
        },
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
  @DataSet(BASE_DATASET + "input/employee-user-registered.yml")
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
  @DataSet(BASE_DATASET + "input/employee-user-registered.yml")
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
  @DataSet(BASE_DATASET + "input/employee-user-registered.yml")
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
  @DataSet(BASE_DATASET + "input/employee-user-registered.yml")
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
  @DataSet(BASE_DATASET + "input/employee-user-registered.yml")
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

  /**
   * Verifies if {@link AuthController#register(UserCreateRequest, HttpServletResponse)} will create
   * a user when request is valid.
   */
  @Test
  @DataSet(BASE_DATASET + "input/empty.yml")
  @ExpectedDataSet(BASE_DATASET + "expected/register-user.yml")
  void shouldRegisterWhenRequestIsValid() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/auth/request/register-finance-user.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/register";

    when(passwordEncoder.encode(anyString())).thenReturn("fakehashedpassword");
    when(jwtUtil.generateToken(any(User.class))).thenReturn("fake-jwt-token");

    // when
    var response = restTemplate.exchange(requestPath, HttpMethod.POST,
        new HttpEntity<>(data, headers), LoginResponse.class);

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
        () -> {
          assertNotNull(response.getBody());
          assertNotNull(response.getBody().getToken());
          assertNotNull(response.getBody().getUser());
          assertNotNull(response.getBody().getUser().getId());
          assertEquals("Finance User", response.getBody().getUser().getName());
          assertEquals("finance@ubs.com", response.getBody().getUser().getEmail());
          assertTrue(Objects.requireNonNull(response.getHeaders().getLocation()).toString()
              .contains("/api/users/" + response.getBody().getUser().getId())
          );
        },
        () -> assertNotNull(response.getHeaders().getLocation()),
        () -> assertNotNull(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)),
        () -> assertTrue(
            Objects.requireNonNull(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("jwt_token=")
        )
    );
  }

  /**
   * Verifies if {@link AuthController#register(UserCreateRequest, HttpServletResponse)} will
   * successfully register an EMPLOYEE with a valid manager.
   */
  @Test
  @DataSet(BASE_DATASET + "input/manager-registered.yml")
  @ExpectedDataSet(value = BASE_DATASET + "expected/register-employee-with-manager.yml", ignoreCols = "id")
  void shouldRegisterEmployeeWithManager() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/auth/request/register-employee-with-manager.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/register";

    when(passwordEncoder.encode(anyString())).thenReturn("fakehashedpassword");
    when(jwtUtil.generateToken(any(User.class))).thenReturn("fake-jwt-token");

    // when
    var response = restTemplate.exchange(requestPath, HttpMethod.POST,
        new HttpEntity<>(data, headers), LoginResponse.class);

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
        () -> {
          assertNotNull(response.getBody());
          assertNotNull(response.getBody().getToken());
          assertNotNull(response.getBody().getUser());
          assertEquals("Employee User", response.getBody().getUser().getName());
          assertEquals("employee@ubs.com", response.getBody().getUser().getEmail());
        },
        () -> assertNotNull(response.getHeaders().getLocation()),
        () -> assertNotNull(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)),
        () -> assertTrue(
            Objects.requireNonNull(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("jwt_token=")
        )
    );
  }

  /**
   * Verifies if {@link AuthController#register(UserCreateRequest, HttpServletResponse)} will return
   * 409 when email already exists.
   */
  @Test
  @DataSet(BASE_DATASET + "input/employee-user-registered.yml")
  @ExpectedDataSet(BASE_DATASET + "input/employee-user-registered.yml")
  void shouldReturn409WhenEmailAlreadyExists() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile(
        "__files/auth/request/register-employee-user.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/register";

    when(passwordEncoder.encode(anyString())).thenReturn("fakehashedpassword");

    // when
    var response = restTemplate.exchange(requestPath, HttpMethod.POST,
        new HttpEntity<>(data, headers), String.class);

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
        () -> assertTrue(Objects.requireNonNull(response.getBody())
            .contains("is already registered"))
    );
  }

  /**
   * Verifies if {@link AuthController#register(UserCreateRequest, HttpServletResponse)} will return
   * 400 when trying to register an EMPLOYEE without a manager.
   */
  @Test
  @DataSet(BASE_DATASET + "input/empty.yml")
  void shouldReturn400WhenTryRegisterEmployeeWithoutManager() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile(
        "__files/auth/request/register-employee-without-manager.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/register";

    // when
    ResponseEntity<String> response = restTemplate.exchange(requestPath, HttpMethod.POST,
        new HttpEntity<>(data, headers), String.class);

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
        () -> assertTrue(Objects.requireNonNull(response.getBody())
            .contains("EMPLOYEE requires a manager"))
    );
  }

  /**
   * Verifies if {@link AuthController#register(UserCreateRequest, HttpServletResponse)} will return
   * 400 when email is empty.
   */
  @Test
  @DataSet(BASE_DATASET + "input/empty.yml")
  void shouldReturn400WhenRegisterEmailIsEmpty() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/auth/request/register-empty-email.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/register";

    // when
    ResponseEntity<String> response = restTemplate.exchange(requestPath, HttpMethod.POST,
        new HttpEntity<>(data, headers), String.class);

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
        () -> assertTrue(Objects.requireNonNull(response.getBody())
            .contains("email is required"))
    );
  }

  /**
   * Verifies if {@link AuthController#register(UserCreateRequest, HttpServletResponse)} will return
   * 400 when password is too short.
   */
  @Test
  @DataSet(BASE_DATASET + "input/empty.yml")
  void shouldReturn400WhenRegisterPasswordIsTooShort() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/auth/request/register-short-password.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/register";

    // when
    ResponseEntity<String> response = restTemplate.exchange(requestPath, HttpMethod.POST,
        new HttpEntity<>(data, headers), String.class);

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
        () -> assertTrue(Objects.requireNonNull(response.getBody())
            .contains("password must be at least 6 characters"))
    );
  }

  /**
   * Verifies if {@link AuthController#register(UserCreateRequest, HttpServletResponse)} will return
   * 400 when department does not exist.
   */
  @Test
  @DataSet(BASE_DATASET + "input/empty.yml")
  void shouldReturn404WhenRegisterDepartmentDoesNotExist() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/auth/request/register-invalid-department.json");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String requestPath = endpointPath + "/register";

    when(passwordEncoder.encode(anyString())).thenReturn("fakehashedpassword");

    // when
    ResponseEntity<String> response = restTemplate.exchange(requestPath, HttpMethod.POST,
        new HttpEntity<>(data, headers), String.class);

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
        () -> assertTrue(Objects.requireNonNull(response.getBody())
            .contains("Department not found with id"))
    );
  }

  /**
   * Verifies if {@link AuthController#logout(HttpServletResponse)} will successfully clear the
   * JWT cookie.
   */
  @Test
  @DataSet(BASE_DATASET + "input/employee-user-registered.yml")
  void shouldLogoutAndClearCookie() {
    // given
    final String endpointPath = getPath();
    String requestPath = endpointPath + "/logout";

    HttpHeaders headers = new HttpHeaders();

    // when
    ResponseEntity<Void> response = restTemplate.exchange(
        requestPath,
        HttpMethod.POST,
        new HttpEntity<>(headers),
        Void.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)),
        () -> assertTrue(
            Objects.requireNonNull(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("Max-Age=0")
        )
    );
  }
}
