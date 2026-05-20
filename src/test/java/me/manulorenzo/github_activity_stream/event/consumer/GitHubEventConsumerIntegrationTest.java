package me.manulorenzo.github_activity_stream.event.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import me.manulorenzo.github_activity_stream.entity.GitHubEventEntity;
import me.manulorenzo.github_activity_stream.repository.GitHubEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
class GitHubEventConsumerIntegrationTest {
  @Autowired private GitHubEventRepository gitHubEventRepository;

  @Autowired private GitHubEventConsumer gitHubEventConsumer;

  @BeforeEach
  void setUp() {
    gitHubEventRepository.deleteAll();
  }

  @Test
  void consumeShouldProcessAndPersistValidEvent() {
    String eventJson = validEventJson("1234567890");

    gitHubEventConsumer.consume(eventJson);

    GitHubEventEntity savedEntity =
        gitHubEventRepository.findByGitHubEventId("1234567890").orElseThrow();

    assertThat(savedEntity.getGitHubEventId()).isEqualTo("1234567890");
    assertThat(savedEntity.getType()).isEqualTo("PushEvent");
    assertThat(savedEntity.getRepoName()).isEqualTo("test/repo");
    assertThat(savedEntity.getActorLogin()).isEqualTo("testuser");
    assertThat(savedEntity.getPayload()).isEqualTo(eventJson);
    assertThat(savedEntity.isProcessed()).isTrue();
  }

  @Test
  void consumeShouldSkipDuplicateEvent() {
    String eventJson = validEventJson("duplicate-event-id");

    gitHubEventConsumer.consume(eventJson);
    gitHubEventConsumer.consume(eventJson);

    assertThat(gitHubEventRepository.findAll()).hasSize(1);
    assertThat(gitHubEventRepository.findByGitHubEventId("duplicate-event-id"))
        .hasValueSatisfying(savedEntity -> assertThat(savedEntity.isProcessed()).isTrue());
  }

  @Test
  void consumeShouldNotPersistInvalidJson() {
    gitHubEventConsumer.consume("not-json");

    assertThat(gitHubEventRepository.findAll()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("eventsWithoutGithubEventId")
  void consumeShouldNotPersistEventWithoutGithubEventId(String eventJson) {
    gitHubEventConsumer.consume(eventJson);

    assertThat(gitHubEventRepository.findAll()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("eventsMissingRequiredStoredFields")
  void consumeShouldNotPersistEventMissingRequiredStoredFields(String eventJson) {
    gitHubEventConsumer.consume(eventJson);

    assertThat(gitHubEventRepository.findAll()).isEmpty();
  }

  private static Stream<Arguments> eventsWithoutGithubEventId() {
    return Stream.of(
        Arguments.of(
            """
                {
                  "type": "PushEvent",
                  "repo": { "name": "test/repo" },
                  "actor": { "login": "testuser" },
                  "created_at": "2026-05-10T17:35:04Z",
                  "payload": { "key": "value" }
                }
                """),
        Arguments.of(
            """
                {
                  "id": "",
                  "type": "PushEvent",
                  "repo": { "name": "test/repo" },
                  "actor": { "login": "testuser" },
                  "created_at": "2026-05-10T17:35:04Z",
                  "payload": { "key": "value" }
                }
                """));
  }

  private static Stream<Arguments> eventsMissingRequiredStoredFields() {
    return Stream.of(
        Arguments.of(
            """
                {
                  "id": "missing-repo",
                  "type": "PushEvent",
                  "actor": { "login": "testuser" },
                  "created_at": "2026-05-10T17:35:04Z",
                  "payload": { "key": "value" }
                }
                """),
        Arguments.of(
            """
                {
                  "id": "missing-actor",
                  "type": "PushEvent",
                  "repo": { "name": "test/repo" },
                  "created_at": "2026-05-10T17:35:04Z",
                  "payload": { "key": "value" }
                }
                """),
        Arguments.of(
            """
                {
                  "id": "missing-type",
                  "repo": { "name": "test/repo" },
                  "actor": { "login": "testuser" },
                  "created_at": "2026-05-10T17:35:04Z",
                  "payload": { "key": "value" }
                }
                """),
        Arguments.of(
            """
                {
                  "id": "missing-created-at",
                  "type": "PushEvent",
                  "repo": { "name": "test/repo" },
                  "actor": { "login": "testuser" },
                  "payload": { "key": "value" }
                }
                """),
        Arguments.of(
            """
                {
                  "id": "invalid-created-at",
                  "type": "PushEvent",
                  "repo": { "name": "test/repo" },
                  "actor": { "login": "testuser" },
                  "created_at": "invalid-date",
                  "payload": { "key": "value" }
                }
                """));
  }

  private String validEventJson(String id) {
    return """
        {
          "id": "%s",
          "type": "PushEvent",
          "repo": { "name": "test/repo" },
          "actor": { "login": "testuser" },
          "created_at": "2026-05-10T17:35:04Z",
          "payload": { "key": "value" }
        }
        """
        .formatted(id);
  }
}
