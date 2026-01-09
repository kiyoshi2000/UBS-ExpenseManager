package com.ubs.expensemanager.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.ubs.expensemanager.config.TestSecurityConfig;
import com.ubs.expensemanager.dto.response.ExpenseResponse;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.security.JwtUtil;
import java.math.BigDecimal;
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
 * Integration test for {@link ExpenseController}
 */
@Import(TestSecurityConfig.class)
public class ExpenseControllerAPITest extends ControllerAPITest {

  private static final String BASE_DATASET = "datasets/expense/";

  @Autowired
  private JwtUtil jwtUtil;

  private HttpHeaders headers;

  @BeforeEach
  void init() {
    basePath = "http://localhost:%d/api/expenses";
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
  }

  // ==================== EMPLOYEE TESTS ====================

  /**
   * Verifies if {@link ExpenseController#findAll} will successfully return all own expenses from
   * employee.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturnAllOwnExpensesFromEmployee() {
    // given
    final String endpointPath = getPath();
    authenticateAsEmployee();

    // when
    ResponseEntity<RestResponsePage<ExpenseResponse>> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.GET,
        new HttpEntity<>(headers),
        new ParameterizedTypeReference<RestResponsePage<ExpenseResponse>>() {}
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> {
          assertNotNull(response.getBody());
          assertEquals(5, response.getBody().getContent().size());
          response.getBody().getContent().forEach(expense ->
              assertEquals(104L, expense.getUserId(), "Employee should only see own expenses")
          );
        }
    );
  }

  /**
   * Verifies if {@link ExpenseController#findById} will successfully return expense when employee
   * is the owner.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturnExpenseWhenEmployeeIsOwner() {
    // given
    final String endpointPath = getPath() + "/101";
    authenticateAsEmployee();

    // when
    ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.GET,
        new HttpEntity<>(headers),
        ExpenseResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> {
          assertNotNull(response.getBody());
          assertEquals(101L, response.getBody().getId());
          assertEquals(104L, response.getBody().getUserId());
        }
    );
  }

  /**
   * Verifies if {@link ExpenseController#create} will successfully create expense when employee
   * provides valid data.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "expected/after-create-expense.yml")
  void shouldCreateExpenseWhenEmployeeProvidedValidData() {
    // given
    final String endpointPath = getPath();
    final String data = readFixtureFile("__files/expense/request/create-expense.json");
    authenticateAsEmployee();

    // when
    ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.POST,
        new HttpEntity<>(data, headers),
        ExpenseResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals(new BigDecimal("25.50"), Objects.requireNonNull(response.getBody()).getAmount()),
        () -> {
          assertNotNull(response.getBody());
          assertEquals("PENDING", response.getBody().getStatus().toString());
          assertNotNull(response.getBody().getUserId());
        },
        () -> assertNotNull(response.getHeaders().getLocation())
    );
  }

  /**
   * Verifies if {@link ExpenseController#update} will successfully update expense when employee
   * owns it and status is PENDING.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "expected/after-update-expense.yml")
  void shouldUpdateExpenseWhenEmployeeOwnsItAndStatusIsPending() {
    // given
    final String endpointPath = getPath() + "/101";
    final String data = readFixtureFile("__files/expense/request/update-expense.json");
    authenticateAsEmployee();

    // when
    ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PUT,
        new HttpEntity<>(data, headers),
        ExpenseResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals(new BigDecimal("60.00"), Objects.requireNonNull(response.getBody()).getAmount()),
        () -> assertEquals("Updated lunch expense", response.getBody().getDescription())
    );
  }

  /**
   * Verifies if {@link ExpenseController#update} will return 400 when employee tries to update
   * approved expense.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturn400WhenEmployeeTriesToUpdateApprovedExpense() {
    // given
    final String endpointPath = getPath() + "/102";
    final String data = readFixtureFile("__files/expense/request/update-expense.json");
    authenticateAsEmployee();

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath,
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
   * Verifies if {@link ExpenseController#delete} will successfully delete expense when employee
   * owns it and status is PENDING.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "expected/after-delete-expense.yml")
  void shouldDeleteExpenseWhenEmployeeOwnsItAndStatusIsPending() {
    // given
    final String endpointPath = getPath() + "/101";
    authenticateAsEmployee();

    // when
    ResponseEntity<Void> response = restTemplate.exchange(
        endpointPath,
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
   * Verifies if {@link ExpenseController#delete} will return 400 when employee tries to delete
   * approved expense.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturn400WhenEmployeeTriesToDeleteApprovedExpense() {
    // given
    final String endpointPath = getPath() + "/102";
    authenticateAsEmployee();

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.DELETE,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link ExpenseController#approve} will return 403 when employee tries to approve
   * expense.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturn403WhenEmployeeTriesToApproveExpense() {
    // given
    final String endpointPath = getPath() + "/101/approve";
    authenticateAsEmployee();

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode())
    );
  }

  // ==================== MANAGER TESTS ====================

  /**
   * Verifies if {@link ExpenseController#findAll} will return all expenses when manager requests.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturnAllExpensesWhenManagerRequests() {
    // given
    final String endpointPath = getPath();
    authenticateAsManager();

    // when
    ResponseEntity<RestResponsePage<ExpenseResponse>> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.GET,
        new HttpEntity<>(headers),
        new ParameterizedTypeReference<RestResponsePage<ExpenseResponse>>() {}
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals(5, Objects.requireNonNull(response.getBody()).getContent().size())
    );
  }

  /**
   * Verifies if {@link ExpenseController#approve} will successfully approve expense when manager
   * is from same department and expense is PENDING.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expense-only-employee-and-manager.yml")
  @ExpectedDataSet(BASE_DATASET + "expected/after-manager-approves-expense.yml")
  void shouldApproveExpenseWhenManagerFromSameDepartmentAndExpenseIsPending() {
    // given
    final String endpointPath = getPath() + "/101/approve";
    authenticateAsManager();

    // when
    ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        ExpenseResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals("APPROVED_BY_MANAGER", Objects.requireNonNull(response.getBody()).getStatus().toString())
    );
  }

  /**
   * Verifies if {@link ExpenseController#approve} will return 403 when manager from different
   * department tries to approve expense.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturn403WhenManagerFromDifferentDepartmentTriesToApprove() {
    // given
    final String endpointPath = getPath() + "/101/approve";
    authenticateAsManagerFromDifferentDepartment();

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link ExpenseController#approve} will return 400 when manager tries to approve
   * already approved expense.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturn400WhenManagerTriesToApproveAlreadyApprovedExpense() {
    // given
    final String endpointPath = getPath() + "/102/approve";
    authenticateAsManager();

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link ExpenseController#reject} will successfully reject expense when manager
   * is from same department.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expense-only-employee-and-manager.yml")
  @ExpectedDataSet(BASE_DATASET + "expected/after-manager-rejects-expense.yml")
  void shouldRejectExpenseWhenManagerFromSameDepartment() {
    // given
    final String endpointPath = getPath() + "/101/reject";
    authenticateAsManager();

    // when
    ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        ExpenseResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals("REJECTED", Objects.requireNonNull(response.getBody()).getStatus().toString())
    );
  }

  /**
   * Verifies if {@link ExpenseController#reject} will return 403 when manager from different
   * department tries to reject expense.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturn403WhenManagerFromDifferentDepartmentTriesToReject() {
    // given
    final String endpointPath = getPath() + "/101/reject";
    authenticateAsManagerFromDifferentDepartment();

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link ExpenseController#requestRevision} will successfully request revision when
   * manager is from same department.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expense-only-employee-and-manager.yml")
  @ExpectedDataSet(BASE_DATASET + "expected/after-manager-requests-revision.yml")
  void shouldRequestRevisionWhenManagerFromSameDepartment() {
    // given
    final String endpointPath = getPath() + "/101/request-revision";
    authenticateAsManager();

    // when
    ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        ExpenseResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals("REQUIRES_REVISION", Objects.requireNonNull(response.getBody()).getStatus().toString())
    );
  }

  /**
   * Verifies if {@link ExpenseController#requestRevision} will return 403 when manager from
   * different department tries to request revision.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturn403WhenManagerFromDifferentDepartmentTriesToRequestRevision() {
    // given
    final String endpointPath = getPath() + "/101/request-revision";
    authenticateAsManagerFromDifferentDepartment();

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode())
    );
  }

  // ==================== FINANCE TESTS ====================

  /**
   * Verifies if {@link ExpenseController#findAll} will return all expenses when finance requests.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturnAllExpensesWhenFinanceRequests() {
    // given
    final String endpointPath = getPath();
    authenticateAsFinance();

    // when
    ResponseEntity<RestResponsePage<ExpenseResponse>> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.GET,
        new HttpEntity<>(headers),
        new ParameterizedTypeReference<RestResponsePage<ExpenseResponse>>() {}
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals(5, Objects.requireNonNull(response.getBody()).getContent().size())
    );
  }

  /**
   * Verifies if {@link ExpenseController#approve} will successfully approve expense when finance
   * approves APPROVED_BY_MANAGER expense.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expense-approved-by-manager.yml")
  @ExpectedDataSet(BASE_DATASET + "expected/after-finance-approves-expense.yml")
  void shouldApproveExpenseWhenFinanceApprovesApprovedByManagerExpense() {
    // given
    final String endpointPath = getPath() + "/101/approve";
    authenticateAsFinance();

    // when
    ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        ExpenseResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals("APPROVED_BY_FINANCE", Objects.requireNonNull(response.getBody()).getStatus().toString())
    );
  }

  /**
   * Verifies if {@link ExpenseController#approve} will return 400 when finance tries to approve
   * PENDING expense.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturn400WhenFinanceTriesToApprovePendingExpense() {
    // given
    final String endpointPath = getPath() + "/101/approve";
    authenticateAsFinance();

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
    );
  }

  /**
   * Verifies if {@link ExpenseController#reject} will successfully reject expense when finance
   * rejects APPROVED_BY_MANAGER expense.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expense-approved-by-manager.yml")
  @ExpectedDataSet(BASE_DATASET + "expected/after-finance-rejects-expense.yml")
  void shouldRejectExpenseWhenFinanceRejectsApprovedByManagerExpense() {
    // given
    final String endpointPath = getPath() + "/101/reject";
    authenticateAsFinance();

    // when
    ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        ExpenseResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals("REJECTED", Objects.requireNonNull(response.getBody()).getStatus().toString())
    );
  }

  /**
   * Verifies if {@link ExpenseController#requestRevision} will successfully request revision when
   * finance requests for any expense (any department).
   */
  @Test
  @DataSet(BASE_DATASET + "input/expense.yml")
  @ExpectedDataSet(BASE_DATASET + "expected/after-finance-requests-revision.yml")
  void shouldRequestRevisionWhenFinanceRequestsForAnyDepartment() {
    // given
    final String endpointPath = getPath() + "/101/request-revision";
    authenticateAsFinance();

    // when
    ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        ExpenseResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals("REQUIRES_REVISION", Objects.requireNonNull(response.getBody()).getStatus().toString())
    );
  }

  /**
   * Verifies if {@link ExpenseController#requestRevision} will return 400 when trying to request
   * revision for already APPROVED_BY_FINANCE expense.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expenses.yml")
  @ExpectedDataSet(BASE_DATASET + "input/expenses.yml")
  void shouldReturn400WhenTryingToRequestRevisionForApprovedByFinanceExpense() {
    // given
    final String endpointPath = getPath() + "/104/request-revision";
    authenticateAsFinance();

    // when
    ResponseEntity<String> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        String.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
    );
  }

  // ==================== WORKFLOW TESTS ====================

  /**
   * Verifies if {@link ExpenseController#update} will reset status to PENDING when employee
   * updates REQUIRES_REVISION expense.
   */
  @Test
  @DataSet(BASE_DATASET + "input/expense.yml")
  @ExpectedDataSet(BASE_DATASET + "expected/after-employee-updates-requires-revision-expense.yml")
  void shouldResetStatusToPendingWhenEmployeeUpdatesRequiresRevisionExpense() {
    // given
    final String endpointPath = getPath() + "/101";
    final String data = readFixtureFile("__files/expense/request/update-expense.json");
    authenticateAsEmployee();

    // when
    ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
        endpointPath,
        HttpMethod.PUT,
        new HttpEntity<>(data, headers),
        ExpenseResponse.class
    );

    // then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals("PENDING", Objects.requireNonNull(response.getBody()).getStatus().toString())
    );
  }

  // ==================== HELPER METHODS ====================

  private void authenticateAsEmployee() {
    // Create minimal User object just for token generation
    // The real user will be loaded from database by UserDetailsService
    User employee = User.builder()
        .email("employee@ubs.com")
        .build();

    String token = jwtUtil.generateToken(employee);
    headers.set("Authorization", "Bearer " + token);
  }

  private void authenticateAsManager() {
    // Create minimal User object just for token generation
    // The real user will be loaded from database by UserDetailsService
    User manager = User.builder()
        .email("manager@ubs.com")
        .build();

    String token = jwtUtil.generateToken(manager);
    headers.set("Authorization", "Bearer " + token);
  }

  private void authenticateAsManagerFromDifferentDepartment() {
    // Create minimal User object just for token generation
    // The real user will be loaded from database by UserDetailsService
    User manager = User.builder()
        .email("manager2@ubs.com")
        .build();

    String token = jwtUtil.generateToken(manager);
    headers.set("Authorization", "Bearer " + token);
  }

  private void authenticateAsFinance() {
    // Create minimal User object just for token generation
    // The real user will be loaded from database by UserDetailsService
    User finance = User.builder()
        .email("finance@ubs.com")
        .build();

    String token = jwtUtil.generateToken(finance);
    headers.set("Authorization", "Bearer " + token);
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
