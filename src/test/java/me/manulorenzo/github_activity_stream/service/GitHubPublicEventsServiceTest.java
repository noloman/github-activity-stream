package me.manulorenzo.github_activity_stream.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import me.manulorenzo.github_activity_stream.config.GitHubPublicEventsProperties;
import me.manulorenzo.github_activity_stream.domain.GitHubEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class GitHubPublicEventsServiceTest {
  @Mock private KafkaTemplate<String, String> kafkaTemplate;
  @Mock private RestTemplate restTemplate;

  private GitHubPublicEventsService gitHubPublicEventsService;
  private GitHubPublicEventsProperties gitHubPublicEventsProperties;

  @BeforeEach
  void setUp() {
    gitHubPublicEventsProperties = new GitHubPublicEventsProperties();
    gitHubPublicEventsProperties.setApiUrl("https://api.github.com/events");
    gitHubPublicEventsService =
        new GitHubPublicEventsService(
            kafkaTemplate, restTemplate, new ObjectMapper(), gitHubPublicEventsProperties);
  }

  @Test
  void fetchPublicEventsPublishesFetchedEventsToKafka() {
    GitHubEvent event =
        GitHubEvent.builder()
            .id("123")
            .type("PushEvent")
            .repo(GitHubEvent.Repo.builder().name("test/repo").build())
            .actor(GitHubEvent.Actor.builder().login("testuser").build())
            .build();
    when(restTemplate.getForEntity(gitHubPublicEventsProperties.getApiUrl(), GitHubEvent[].class))
        .thenReturn(ResponseEntity.ok(new GitHubEvent[] {event}));

    gitHubPublicEventsService.fetchPublicEvents();

    verify(restTemplate)
        .getForEntity(gitHubPublicEventsProperties.getApiUrl(), GitHubEvent[].class);
    ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
    verify(kafkaTemplate)
        .send(eq("github-public-events"), eq("PushEvent"), payloadCaptor.capture());

    assertThat(payloadCaptor.getValue()).contains("\"id\":\"123\"");
    assertThat(payloadCaptor.getValue()).contains("\"type\":\"PushEvent\"");
    assertThat(payloadCaptor.getValue()).contains("\"name\":\"test/repo\"");
    assertThat(payloadCaptor.getValue()).contains("\"login\":\"testuser\"");
  }

  @Test
  void fetchPublicEventsDoesNotPublishWhenResponseBodyIsNull() {
    when(restTemplate.getForEntity(gitHubPublicEventsProperties.getApiUrl(), GitHubEvent[].class))
        .thenReturn(ResponseEntity.ok(null));

    gitHubPublicEventsService.fetchPublicEvents();

    verify(restTemplate)
        .getForEntity(gitHubPublicEventsProperties.getApiUrl(), GitHubEvent[].class);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void fetchPublicEventsDoesNotPublishWhenResponseBodyIsEmpty() {
    when(restTemplate.getForEntity(gitHubPublicEventsProperties.getApiUrl(), GitHubEvent[].class))
        .thenReturn(ResponseEntity.ok(new GitHubEvent[] {}));

    gitHubPublicEventsService.fetchPublicEvents();

    verify(restTemplate)
        .getForEntity(gitHubPublicEventsProperties.getApiUrl(), GitHubEvent[].class);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void fetchPublicEventsBacksOffAfterRetryAfterRateLimitResponse() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.RETRY_AFTER, "60");
    HttpClientErrorException rateLimitException =
        HttpClientErrorException.create(
            HttpStatus.FORBIDDEN,
            "rate limited",
            headers,
            "rate limit exceeded".getBytes(StandardCharsets.UTF_8),
            null);
    when(restTemplate.getForEntity(gitHubPublicEventsProperties.getApiUrl(), GitHubEvent[].class))
        .thenThrow(rateLimitException);

    gitHubPublicEventsService.fetchPublicEvents();
    gitHubPublicEventsService.fetchPublicEvents();

    verify(restTemplate)
        .getForEntity(gitHubPublicEventsProperties.getApiUrl(), GitHubEvent[].class);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void fetchPublicEventsContinuesPollingWhenHttpFailureHasNoRetryHeaders() {
    HttpClientErrorException exception =
        HttpClientErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "server error",
            HttpHeaders.EMPTY,
            "temporary failure".getBytes(StandardCharsets.UTF_8),
            null);
    when(restTemplate.getForEntity(gitHubPublicEventsProperties.getApiUrl(), GitHubEvent[].class))
        .thenThrow(exception);

    gitHubPublicEventsService.fetchPublicEvents();
    gitHubPublicEventsService.fetchPublicEvents();

    verify(restTemplate, times(2))
        .getForEntity(gitHubPublicEventsProperties.getApiUrl(), GitHubEvent[].class);
    verifyNoInteractions(kafkaTemplate);
  }
}
