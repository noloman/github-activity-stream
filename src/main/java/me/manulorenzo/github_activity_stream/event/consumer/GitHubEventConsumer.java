package me.manulorenzo.github_activity_stream.event.consumer;

import java.time.Instant;
import me.manulorenzo.github_activity_stream.domain.GitHubEvent;
import me.manulorenzo.github_activity_stream.dto.GitHubEventResponseDto;
import me.manulorenzo.github_activity_stream.entity.GitHubEventEntity;
import me.manulorenzo.github_activity_stream.mapper.GitHubEventMapper;
import me.manulorenzo.github_activity_stream.repository.GitHubEventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class GitHubEventConsumer {
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final GitHubEventRepository gitHubEventRepository;
  private final ObjectMapper objectMapper;
  private final GitHubEventMapper gitHubEventMapper;

  public GitHubEventConsumer(
      SimpMessagingTemplate simpMessagingTemplate,
      GitHubEventRepository gitHubEventRepository,
      ObjectMapper objectMapper,
      GitHubEventMapper gitHubEventMapper) {
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.gitHubEventRepository = gitHubEventRepository;
    this.objectMapper = objectMapper;
    this.gitHubEventMapper = gitHubEventMapper;
  }

  @KafkaListener(topics = "github-public-events")
  public void consume(String eventJson) {
    try {
      GitHubEvent gitHubEvent = objectMapper.readValue(eventJson, GitHubEvent.class);
      GitHubEventEntity gitHubEventEntity =
          gitHubEventMapper.toEntity(gitHubEvent, eventJson, Instant.now());
      gitHubEventRepository.save(gitHubEventEntity);
      GitHubEventResponseDto gitHubEventResponseDto =
          gitHubEventMapper.toResponseDto(gitHubEventEntity);
      simpMessagingTemplate.convertAndSend("/topic/github-events", gitHubEventResponseDto);
    } catch (Exception e) {
      System.err.println("Error processing event: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
