package me.manulorenzo.github_activity_stream.service;

import me.manulorenzo.github_activity_stream.dto.GitHubEventResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface GitHubEventService {
    Page<GitHubEventResponseDto> getAllEvents(int page, int size);

    List<GitHubEventResponseDto> getEventsByRepo(String repoName);

    List<GitHubEventResponseDto> getEventsByType(String type);

    List<GitHubEventResponseDto> getRecentEvents(int hours);

    List<GitHubEventResponseDto> getUnprocessedEvents();
}
