package me.manulorenzo.github_activity_stream.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import me.manulorenzo.github_activity_stream.config.GitHubPublicEventsProperties;
import me.manulorenzo.github_activity_stream.domain.GitHubEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@ConditionalOnProperty(
    prefix = "github.public-events",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class GitHubPublicEventsService {
  private static final String TOPIC_NAME = "github-public-events";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final GitHubPublicEventsProperties gitHubPublicEventsProperties;
  private volatile Instant rateLimitCooldownUntil;

  public GitHubPublicEventsService(
      KafkaTemplate<String, String> kafkaTemplate,
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      GitHubPublicEventsProperties gitHubPublicEventsProperties) {
    this.kafkaTemplate = kafkaTemplate;
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
    this.gitHubPublicEventsProperties = gitHubPublicEventsProperties;
  }

  @Scheduled(fixedRateString = "#{@gitHubPublicEventsProperties.pollInterval.toMillis()}")
  public void fetchPublicEvents() {
    if (isCoolingDown()) {
      log.info("Skipping GitHub events poll until {} due to rate limiting", rateLimitCooldownUntil);
      return;
    }

    try {
      ResponseEntity<GitHubEvent[]> response =
          restTemplate.getForEntity(gitHubPublicEventsProperties.getApiUrl(), GitHubEvent[].class);

      clearCooldown();

      GitHubEvent[] events = response.getBody();
      if (events != null) {
        Arrays.stream(events)
            .forEach(
                event -> {
                  try {
                    String eventJson = objectMapper.writeValueAsString(event);
                    kafkaTemplate.send(TOPIC_NAME, event.getType(), eventJson);
                  } catch (Exception e) {
                    log.error("Failed to serialize GitHub event for Kafka", e);
                  }
                });
      }
    } catch (HttpStatusCodeException e) {
      handleGitHubHttpFailure(e);
    } catch (Exception e) {
      log.error(
          "Error fetching public events from {}", gitHubPublicEventsProperties.getApiUrl(), e);
    }
  }

  private boolean isCoolingDown() {
    return rateLimitCooldownUntil != null && Instant.now().isBefore(rateLimitCooldownUntil);
  }

  private void clearCooldown() {
    rateLimitCooldownUntil = null;
  }

  private void handleGitHubHttpFailure(HttpStatusCodeException e) {
    Optional<Instant> cooldownUntil = resolveCooldownUntil(e.getResponseHeaders());
    cooldownUntil.ifPresent(instant -> rateLimitCooldownUntil = instant);

    String responseBody = e.getResponseBodyAsString();
    if (cooldownUntil.isPresent()) {
      log.warn(
          "GitHub API request failed with status {}. Backing off until {}. Response: {}",
          e.getStatusCode(),
          cooldownUntil.get(),
          responseBody);
      return;
    }

    log.warn(
        "GitHub API request failed with status {} and no retry headers. Response: {}",
        e.getStatusCode(),
        responseBody);
  }

  private Optional<Instant> resolveCooldownUntil(HttpHeaders headers) {
    if (headers == null) {
      return Optional.empty();
    }

    String retryAfter = headers.getFirst(HttpHeaders.RETRY_AFTER);
    if (StringUtils.hasText(retryAfter)) {
      try {
        long seconds = Long.parseLong(retryAfter.trim());
        return Optional.of(Instant.now().plusSeconds(seconds));
      } catch (NumberFormatException ignored) {
        log.warn("Invalid Retry-After header from GitHub: {}", retryAfter);
      }
    }

    String rateLimitReset = headers.getFirst("X-RateLimit-Reset");
    if (StringUtils.hasText(rateLimitReset)) {
      try {
        return Optional.of(Instant.ofEpochSecond(Long.parseLong(rateLimitReset.trim())));
      } catch (NumberFormatException ignored) {
        log.warn("Invalid X-RateLimit-Reset header from GitHub: {}", rateLimitReset);
      }
    }

    return Optional.empty();
  }
}
