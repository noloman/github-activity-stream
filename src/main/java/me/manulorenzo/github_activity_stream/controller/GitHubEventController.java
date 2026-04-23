package me.manulorenzo.github_activity_stream.controller;

import me.manulorenzo.github_activity_stream.dto.GitHubEventResponseDto;
import me.manulorenzo.github_activity_stream.service.GitHubEventService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class GitHubEventController {
    private final GitHubEventService gitHubEventService;

    public GitHubEventController(GitHubEventService gitHubEventService) {
        this.gitHubEventService = gitHubEventService;
    }

    @GetMapping
    Page<GitHubEventResponseDto> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return gitHubEventService.getAllEvents(page, size);
    }

    @GetMapping("/repo")
    List<GitHubEventResponseDto> getEventsByRepo(@RequestParam String repoName) {
        return gitHubEventService.getEventsByRepo(repoName);
    }

    @GetMapping("/type")
    List<GitHubEventResponseDto> getEventsByType(@RequestParam String type) {
        return gitHubEventService.getEventsByType(type);
    }

    @GetMapping("/recent")
    List<GitHubEventResponseDto> getRecentEvents(@RequestParam(defaultValue = "1") int hours) {
        return gitHubEventService.getRecentEvents(hours);
    }

    @GetMapping("/unprocessed")
    List<GitHubEventResponseDto> getUnprocessedEvents() {
        return gitHubEventService.getUnprocessedEvents();
    }
}
