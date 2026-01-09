package com.ubs.expensemanager.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.ubs.expensemanager.dto.response.ExpenseCategoryResponse;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
 * Integration test for {@link ExpenseCategoryController}
 */
public class ExpenseCategoryAPITest extends ControllerAPITest {

    private static final String BASE_DATASET = "datasets/expense-categories/";

    @BeforeEach
    void init() {
        basePath = "http://localhost:%d/api/expense-categories";
    }

    /**
     * Verifies if {@link ExpenseCategoryController#create} will successfully
     * create an expense category when the request is valid.
     */
    @Test
    @DataSet(BASE_DATASET + "input/empty.yml")
    @ExpectedDataSet(value = BASE_DATASET + "expected/category-created.yml", ignoreCols = "id")
    void shouldCreateCategoryWhenRequestIsValid() {
        // given
        final String endpointPath = getPath();
        final String data = readFixtureFile("__files/expense-categories/request/create-category-valid.json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<ExpenseCategoryResponse> response = restTemplate.exchange(
                endpointPath,
                HttpMethod.POST,
                new HttpEntity<>(data, headers),
                ExpenseCategoryResponse.class
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> {
                    assertNotNull(response.getBody());
                    assertEquals("Food", response.getBody().getName());
                    assertEquals(100.00, response.getBody().getDailyBudget().doubleValue());
                    assertEquals(3000.00, response.getBody().getMonthlyBudget().doubleValue());
                    assertEquals("USD", response.getBody().getCurrencyName());
                    assertNotNull(response.getBody().getId());
                },
                () -> assertNotNull(response.getHeaders().getLocation()),
                () -> assertTrue(
                        Objects.requireNonNull(response.getHeaders().getLocation()).toString()
                                .contains("/api/expense-categories/")
                )
        );
    }

    /**
     * Verifies if {@link ExpenseCategoryController#create} will return 400
     * when the category name is empty.
     */
    @Test
    @DataSet(BASE_DATASET + "input/empty.yml")
    void shouldReturn400WhenCategoryNameIsEmpty() {
        // given
        final String endpointPath = getPath();
        final String data = readFixtureFile("__files/expense-categories/request/create-category-empty-name.json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                endpointPath,
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
     * Verifies if {@link ExpenseCategoryController#create} will return 400
     * when the daily budget is negative.
     */
    @Test
    @DataSet(BASE_DATASET + "input/empty.yml")
    void shouldReturn400WhenDailyBudgetIsNegative() {
        // given
        final String endpointPath = getPath();
        final String data = readFixtureFile("__files/expense-categories/request/create-category-negative-budget.json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                endpointPath,
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
     * Verifies if {@link ExpenseCategoryController#create} will return 409
     * when a category with the same name already exists.
     */
    @Test
    @DataSet(BASE_DATASET + "input/category-exists.yml")
    void shouldReturn409WhenCategoryAlreadyExists() {
        // given
        final String endpointPath = getPath();
        final String data = readFixtureFile("__files/expense-categories/request/create-category-valid.json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                endpointPath,
                HttpMethod.POST,
                new HttpEntity<>(data, headers),
                String.class
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertTrue(Objects.requireNonNull(response.getBody())
                        .contains("already exists"))
        );
    }

    /**
     * Verifies if {@link ExpenseCategoryController#listAll} will successfully
     * retrieve all expense categories.
     */
    @Test
    @DataSet(BASE_DATASET + "input/category-exists.yml")
    void shouldListAllCategories() {
        // given
        final String endpointPath = getPath();

        // when
        ResponseEntity<RestResponsePage<ExpenseCategoryResponse>> response = restTemplate.exchange(
                endpointPath,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<RestResponsePage<ExpenseCategoryResponse>>() {}
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(1, Objects.requireNonNull(response.getBody()).getContent().size()),
                () -> {
                    assertNotNull(response.getBody());
                    ExpenseCategoryResponse category = response.getBody().getContent().getFirst();
                    assertEquals("Food", category.getName());
                    assertEquals(100.00, category.getDailyBudget().doubleValue());
                    assertEquals(3000.00, category.getMonthlyBudget().doubleValue());
                }
        );
    }

    /**
     * Verifies if {@link ExpenseCategoryController#findById} will successfully
     * retrieve an expense category by its id.
     */
    @Test
    @DataSet(BASE_DATASET + "input/category-exists.yml")
    void shouldFindCategoryById() {
        // given
        final String endpointPath = getPath();
        final long categoryId = 1L;

        // when
        ResponseEntity<ExpenseCategoryResponse> response = restTemplate.exchange(
                endpointPath + "/" + categoryId,
                HttpMethod.GET,
                null,
                ExpenseCategoryResponse.class
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> {
                    ExpenseCategoryResponse category = response.getBody();
                    assertNotNull(category);
                    assertEquals(1L, category.getId());
                    assertEquals("Food", category.getName());
                    assertEquals(100.00, category.getDailyBudget().doubleValue());
                    assertEquals(3000.00, category.getMonthlyBudget().doubleValue());
                }
        );
    }

    /**
     * Verifies if {@link ExpenseCategoryController#findById} will return 404
     * when the category does not exist.
     */
    @Test
    @DataSet(BASE_DATASET + "input/empty.yml")
    void shouldReturn404WhenCategoryNotFound() {
        // given
        final String endpointPath = getPath();
        final long categoryId = 999L;

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                endpointPath + "/" + categoryId,
                HttpMethod.GET,
                null,
                String.class
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode())
        );
    }

    /**
     * Verifies if {@link ExpenseCategoryController#update} will successfully
     * update an expense category when the request is valid.
     */
    @Test
    @DataSet(BASE_DATASET + "input/category-exists.yml")
    void shouldUpdateCategoryWhenRequestIsValid() {
        // given
        final String endpointPath = getPath();
        final long categoryId = 1L;
        final String data = readFixtureFile("__files/expense-categories/request/update-category-valid.json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<ExpenseCategoryResponse> response = restTemplate.exchange(
                endpointPath + "/" + categoryId,
                HttpMethod.PUT,
                new HttpEntity<>(data, headers),
                ExpenseCategoryResponse.class
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> {
                    ExpenseCategoryResponse category = response.getBody();
                    assertNotNull(category);
                    assertEquals(1L, category.getId());
                    assertEquals("Food Updated", category.getName());
                    assertEquals(150.00, category.getDailyBudget().doubleValue());
                    assertEquals(4500.00, category.getMonthlyBudget().doubleValue());
                }
        );
    }

    /**
     * Verifies if {@link ExpenseCategoryController#update} will return 404
     * when the category does not exist.
     */
    @Test
    @DataSet(BASE_DATASET + "input/empty.yml")
    void shouldReturn404WhenUpdatingNonExistentCategory() {
        // given
        final String endpointPath = getPath();
        final long categoryId = 999L;
        final String data = readFixtureFile("__files/expense-categories/request/update-category-valid.json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                endpointPath + "/" + categoryId,
                HttpMethod.PUT,
                new HttpEntity<>(data, headers),
                String.class
        );

        // then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode())
        );
    }

    /**
     * Helper class to deserialize paginated responses from REST API.
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
