package com.ubs.expensemanager.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import com.ubs.expensemanager.config.TestSecurityConfig;
import com.ubs.expensemanager.dto.response.UserResponse;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.security.JwtUtil;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Integration test for {@link UserController}
 */
@Import(TestSecurityConfig.class)
public class UserControllerAPITest extends ControllerAPITest {

  private static final String BASE_DATASET = "datasets/user/";

  @Autowired
  private JwtUtil jwtUtil;

  private HttpHeaders headers;

  @BeforeEach
  void init() {
    basePath = "http://localhost:%d/api/users";
    headers = new HttpHeaders();

    // Stub password encoder to return the input as-is for testing
    when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "encoded_" + inv.getArgument(0));

    // Authenticate as MANAGER by default (managers have access to user management)
    authenticateAsManager();
  }

  /**
   * Authenticate as a MANAGER user for testing.
   */
  private void authenticateAsManager() {
    User manager = User.builder()
        .id(2L)
        .email("manager@ubs.com")
        .name("Manager User")
        .role(UserRole.MANAGER)
        .build();
    String token = jwtUtil.generateToken(manager);
    headers.set("Authorization", "Bearer " + token);
  }

  /**
   * Verifies if {@link UserController#findAll} will successfully retrieve all active users when no
   * filters are applied.
   */
  @Test
  @DataSet(BASE_DATASET + "input/multiple-users.yml")
  void shouldReturnAllUsersWhenNoFilters() {
    // given
    final String endpointPath = getPath();

    // when
    ResponseEntity<RestResponsePage<UserResponse>> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.GET,
        new HttpEntity<>(headers),
        new ParameterizedTypeReference<>() {
        }
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals(3, Objects.requireNonNull(response.getBody()).getContent().size()),
        () -> {
          assertNotNull(response.getBody());
          List<UserResponse> users = response.getBody().getContent();
          assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("finance@ubs.com")));
          assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("manager@ubs.com")));
          assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("employee@ubs.com")));
        }
    );
  }

  /**
   * Verifies if {@link UserController#findById} will successfully retrieve a user when the ID
   * exists.
   */
  @Test
  @DataSet(BASE_DATASET + "input/multiple-users.yml")
  void shouldReturnUserWhenIdExists() {
    // given
    final String endpointPath = getPath();
    final long userId = 3L;

    // when
    ResponseEntity<UserResponse> response = restTemplate.exchange(
        endpointPath + "/" + userId,
        HttpMethod.GET,
        new HttpEntity<>(headers),
        UserResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> {
          UserResponse user = response.getBody();
          assertNotNull(user);
          assertEquals(3L, user.getId());
          assertEquals("employee@ubs.com", user.getEmail());
          assertEquals("IT Employee", user.getName());
          assertEquals("ROLE_EMPLOYEE", user.getRole());
          assertNotNull(user.getManager());
          assertEquals(2L, user.getManager().getId());
          assertEquals("manager@ubs.com", user.getManager().getEmail());
        }
    );
  }

  /**
   * Verifies if {@link UserController#findById} will return 404 when the user does not exist.
   */
  @Test
  @DataSet(BASE_DATASET + "input/multiple-users.yml")
  void shouldReturn404WhenUserNotFound() {
    // given
    final String endpointPath = getPath();
    final long userId = 999L;

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath + "/" + userId,
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link UserController#update} will successfully update a user when the request is
   * valid.
   */
  @Test
  @DataSet(BASE_DATASET + "input/multiple-users.yml")
  void shouldUpdateUserWhenRequestIsValid() {
    // given
    final String endpointPath = getPath();
    final long userId = 3L;
    final String data = readFixtureFile("__files/user/request/update-valid.json");

    headers.setContentType(MediaType.APPLICATION_JSON);

    // when
    ResponseEntity<UserResponse> response = restTemplate.exchange(
        endpointPath + "/" + userId,
        HttpMethod.PUT,
        new HttpEntity<>(data, headers),
        UserResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> {
          UserResponse user = response.getBody();
          assertNotNull(user);
          assertEquals(3L, user.getId());
          assertEquals("Employee Updated", user.getName());
          assertEquals("employee@ubs.com", user.getEmail());
          assertEquals("ROLE_EMPLOYEE", user.getRole());
        }
    );
  }

  /**
   * Verifies if {@link UserController#update} will return 400 when the email format is invalid.
   */
  @Test
  @DataSet(BASE_DATASET + "input/multiple-users.yml")
  void shouldReturn400WhenEmailFormatIsInvalid() {
    // given
    final String endpointPath = getPath();
    final long userId = 3L;
    final String data = readFixtureFile("__files/user/request/update-invalid-email.json");

    headers.setContentType(MediaType.APPLICATION_JSON);

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath + "/" + userId,
        HttpMethod.PUT,
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
   * Verifies if {@link UserController#update} will return 400 when an EMPLOYEE role has no manager
   * assigned.
   */
  @Test
  @DataSet(BASE_DATASET + "input/multiple-users.yml")
  void shouldReturn400WhenEmployeeHasNoManager() {
    // given
    final String endpointPath = getPath();
    final long userId = 3L;
    final String data = readFixtureFile("__files/user/request/update-employee-no-manager.json");

    headers.setContentType(MediaType.APPLICATION_JSON);

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath + "/" + userId,
        HttpMethod.PUT,
        new HttpEntity<>(data, headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
        () -> assertTrue(Objects.requireNonNull(response.getBody())
            .toLowerCase().contains("manager"))
    );
  }

  /**
   * Verifies if {@link UserController#delete} will successfully deactivate a user when valid.
   */
  @Test
  @DataSet(BASE_DATASET + "input/multiple-users.yml")
  void shouldDeactivateUserWhenValid() {
    // given
    final String endpointPath = getPath();
    final long userId = 3L;

    // when
    ResponseEntity<Void> response = restTemplate.exchange(
        endpointPath + "/" + userId,
        HttpMethod.DELETE,
        new HttpEntity<>(headers),
        Void.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link UserController#delete} will return 404 when the user does not exist.
   */
  @Test
  @DataSet(BASE_DATASET + "input/multiple-users.yml")
  void shouldReturn404WhenDeletingNonExistentUser() {
    // given
    final String endpointPath = getPath();
    final long userId = 999L;

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath + "/" + userId,
        HttpMethod.DELETE,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link UserController#reactivate} will successfully reactivate an inactive user.
   */
  @Test
  @DataSet(BASE_DATASET + "input/inactive-user.yml")
  void shouldReactivateUserWhenInactive() {
    // given
    final String endpointPath = getPath();
    final long userId = 2L;

    // when
    ResponseEntity<UserResponse> response = restTemplate.exchange(
        endpointPath + "/" + userId + "/reactivate",
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        UserResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> {
          UserResponse user = response.getBody();
          assertNotNull(user);
          assertEquals(2L, user.getId());
          assertEquals("inactive@ubs.com", user.getEmail());
          assertTrue(user.isActive());
        }
    );
  }

  /**
   * Verifies if {@link UserController#reactivate} will return 400 when the user is already active.
   */
  @Test
  @DataSet(BASE_DATASET + "input/inactive-user.yml")
  void shouldReturn400WhenUserAlreadyActive() {
    // given
    final String endpointPath = getPath();
    final long userId = 1L; // Active manager

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath + "/" + userId + "/reactivate",
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
        () -> assertTrue(Objects.requireNonNull(response.getBody())
            .toLowerCase().contains("active"))
    );
  }

  /**
   * Custom Page implementation for deserializing Spring Data Page responses from REST endpoints in
   * integration tests.
   *
   * <p>Jackson cannot directly deserialize {@link org.springframework.data.domain.Page}
   * because it's an interface. This class provides a concrete implementation that can be used with
   * {@link ParameterizedTypeReference}.</p>
   */
  static class RestResponsePage<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestResponsePage(@JsonProperty("content") List<T> content,
        @JsonProperty("number") int number,
        @JsonProperty("size") int size,
        @JsonProperty("totalElements") Long totalElements,
        @JsonProperty("pageable") JsonNode pageable,
        @JsonProperty("last") boolean last,
        @JsonProperty("totalPages") int totalPages,
        @JsonProperty("sort") JsonNode sort,
        @JsonProperty("first") boolean first,
        @JsonProperty("numberOfElements") int numberOfElements) {
      super(content, PageRequest.of(number, size), totalElements);
    }

    public RestResponsePage(List<T> content) {
      super(content);
    }
  }
}
