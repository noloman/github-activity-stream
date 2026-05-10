package me.manulorenzo.github_activity_stream.event.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import me.manulorenzo.github_activity_stream.entity.GitHubEventEntity;
import me.manulorenzo.github_activity_stream.repository.GitHubEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
class GitHubEventConsumerIntegrationTest {

    @Autowired
    private GitHubEventRepository gitHubEventRepository;

    @Autowired
    private GitHubEventConsumer gitHubEventConsumer;

    @BeforeEach
    void setUp() {
        gitHubEventRepository.deleteAll();
    }

    @Test
    void consume_shouldProcessAndPersistEventWithProcessedTrue() {
        String eventJson = """
            {
              "id": "1234567890",
              "type": "PushEvent",
              "repo": { "name": "test/repo" },
              "actor": { "login": "testuser" },
              "created_at": "2026-05-10T17:35:04Z",
              "payload": { "key": "value" }
            }
            """;

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
    void consume_shouldSkipDuplicateEvent() {
        String eventJson = """
            {
              "id": "duplicate-event-id",
              "type": "PushEvent",
              "repo": { "name": "test/repo" },
              "actor": { "login": "testuser" },
              "created_at": "2026-05-10T17:35:04Z",
              "payload": { "key": "value" }
            }
            """;

        gitHubEventConsumer.consume(eventJson);
        gitHubEventConsumer.consume(eventJson);

        assertThat(gitHubEventRepository.findAll()).hasSize(1);

        GitHubEventEntity savedEntity =
            gitHubEventRepository.findByGitHubEventId("duplicate-event-id").orElseThrow();
        assertThat(savedEntity.isProcessed()).isTrue();
    }
}
