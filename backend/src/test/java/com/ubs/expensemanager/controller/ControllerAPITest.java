package com.ubs.expensemanager.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.junit5.api.DBRider;
import com.github.tomakehurst.wiremock.core.Options;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base integration test class with support to in-memory database connection
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = Options.DYNAMIC_PORT)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DBRider
@DBUnit(cacheConnection = false, alwaysCleanBefore = true, alwaysCleanAfter = true, raiseExceptionOnCleanUp = true, escapePattern = "\"", qualifiedTableNames = true, schema = "PUBLIC")
@DirtiesContext
@Testcontainers
public abstract class ControllerAPITest {

  protected String basePath;

  @LocalServerPort
  protected int port;

  @Autowired
  protected TestRestTemplate restTemplate;

  @Autowired
  protected ObjectMapper objectMapper;

  @SuppressWarnings("unused")
  @MockBean
  protected PasswordEncoder passwordEncoder;

  protected static final String BASE_DATASET = "datasets/";

  /**
   * Formats base path with server port
   *
   * @return a string containing URL and port
   */
  protected String getPath() {
    return String.format(basePath, port);
  }

  /**
   * Returns the string content of a file given its content
   *
   * @param filePath file path
   * @return its content as a string
   */
  protected String readFixtureFile(String filePath) {
    try {
      final var expressionParser = new SpelExpressionParser();
      final var json = new ClassPathResource(filePath);
      final var content = new String(json.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      final Pattern pattern = Pattern.compile("\\$\\{([^\\}]+)\\}");
      final Matcher matcher = pattern.matcher(content);
      StringBuilder result = new StringBuilder();
      while (matcher.find()) {
        matcher.appendReplacement(result,
            Optional.ofNullable(
                    expressionParser.parseExpression(matcher.group(1)).getValue(String.class))
                .orElse(""));
      }
      matcher.appendTail(result);
      return result.toString();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

}
