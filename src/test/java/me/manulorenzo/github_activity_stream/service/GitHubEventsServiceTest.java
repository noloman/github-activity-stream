package me.manulorenzo.github_activity_stream.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import me.manulorenzo.github_activity_stream.dto.GitHubEventResponseDto;
import me.manulorenzo.github_activity_stream.entity.GitHubEventEntity;
import me.manulorenzo.github_activity_stream.mapper.GitHubEventMapper;
import me.manulorenzo.github_activity_stream.repository.GitHubEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class GitHubEventsServiceTest {
  @Mock private GitHubEventRepository gitHubEventRepository;
  @Mock private GitHubEventMapper gitHubEventMapper;

  private GitHubEventsServiceImpl gitHubEventsService;

  @BeforeEach
  void setUp() {
    gitHubEventsService = new GitHubEventsServiceImpl(gitHubEventRepository, gitHubEventMapper);
  }

  @Test
  void getAllEventsReturnsMappedPage() {
    GitHubEventEntity entity = new GitHubEventEntity();
    GitHubEventResponseDto dto = responseDto("github-id", "PushEvent", "owner/repo", true);
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(gitHubEventRepository.findAll(pageRequest))
        .thenReturn(new PageImpl<>(List.of(entity), pageRequest, 1));
    when(gitHubEventMapper.toResponseDto(entity)).thenReturn(dto);

    Page<GitHubEventResponseDto> events = gitHubEventsService.getAllEvents(0, 10);

    assertThat(events.getContent()).containsExactly(dto);
    assertThat(events.getTotalElements()).isEqualTo(1);
    assertThat(events.getNumber()).isZero();
    assertThat(events.getSize()).isEqualTo(10);
  }

  @Test
  void getEventsByRepoReturnsMappedDtos() {
    GitHubEventEntity entity = new GitHubEventEntity();
    GitHubEventResponseDto dto = responseDto("github-id", "PushEvent", "owner/repo", true);

    when(gitHubEventRepository.findByRepoName("owner/repo")).thenReturn(List.of(entity));
    when(gitHubEventMapper.toResponseDtos(List.of(entity))).thenReturn(List.of(dto));

    List<GitHubEventResponseDto> events = gitHubEventsService.getEventsByRepo("owner/repo");

    assertThat(events).containsExactly(dto);
  }

  @Test
  void getEventsByTypeReturnsMappedDtos() {
    GitHubEventEntity entity = new GitHubEventEntity();
    GitHubEventResponseDto dto = responseDto("github-id", "PullRequestEvent", "owner/repo", true);

    when(gitHubEventRepository.findByType("PullRequestEvent")).thenReturn(List.of(entity));
    when(gitHubEventMapper.toResponseDtos(List.of(entity))).thenReturn(List.of(dto));

    List<GitHubEventResponseDto> events = gitHubEventsService.getEventsByType("PullRequestEvent");

    assertThat(events).containsExactly(dto);
  }

  @Test
  void getRecentEventsQueriesEventsCreatedAfterRequestedHours() {
    GitHubEventEntity entity = new GitHubEventEntity();
    GitHubEventResponseDto dto = responseDto("github-id", "PushEvent", "owner/repo", true);
    Instant beforeCall = Instant.now().minus(Duration.ofHours(2));

    when(gitHubEventRepository.findByCreatedAtAfter(any(Instant.class)))
        .thenReturn(List.of(entity));
    when(gitHubEventMapper.toResponseDtos(List.of(entity))).thenReturn(List.of(dto));

    List<GitHubEventResponseDto> events = gitHubEventsService.getRecentEvents(2);

    Instant afterCall = Instant.now().minus(Duration.ofHours(2));
    ArgumentCaptor<Instant> sinceCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(gitHubEventRepository).findByCreatedAtAfter(sinceCaptor.capture());
    assertThat(sinceCaptor.getValue()).isBetween(beforeCall, afterCall);
    assertThat(events).containsExactly(dto);
  }

  @Test
  void getUnprocessedEventsReturnsMappedDtos() {
    GitHubEventEntity entity = new GitHubEventEntity();
    GitHubEventResponseDto dto = responseDto("github-id", "PushEvent", "owner/repo", false);

    when(gitHubEventRepository.findByProcessed(false)).thenReturn(List.of(entity));
    when(gitHubEventMapper.toResponseDtos(List.of(entity))).thenReturn(List.of(dto));

    List<GitHubEventResponseDto> events = gitHubEventsService.getUnprocessedEvents();

    assertThat(events).containsExactly(dto);
  }

  private GitHubEventResponseDto responseDto(
      String gitHubEventId, String type, String repoName, boolean processed) {
    return new GitHubEventResponseDto(
        1L,
        gitHubEventId,
        type,
        repoName,
        "testuser",
        Instant.parse("2026-05-10T17:35:04Z"),
        "{}",
        processed);
  }
}
