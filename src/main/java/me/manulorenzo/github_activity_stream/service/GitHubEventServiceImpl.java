package me.manulorenzo.github_activity_stream.service;

import me.manulorenzo.github_activity_stream.dto.GitHubEventResponseDto;
import me.manulorenzo.github_activity_stream.mapper.GitHubEventMapper;
import me.manulorenzo.github_activity_stream.repository.GitHubEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class GitHubEventServiceImpl implements GitHubEventService {
    private final GitHubEventRepository gitHubEventRepository;
    private final GitHubEventMapper gitHubEventMapper;

    public GitHubEventServiceImpl(GitHubEventRepository gitHubEventRepository, GitHubEventMapper gitHubEventMapper) {
        this.gitHubEventRepository = gitHubEventRepository;
        this.gitHubEventMapper = gitHubEventMapper;
    }

    @Override
    public Page<GitHubEventResponseDto> getAllEvents(int page, int size) {
        return gitHubEventRepository.findAll(PageRequest.of(page, size))
                .map(gitHubEventMapper::toResponseDto);
    }

    @Override
    public List<GitHubEventResponseDto> getEventsByRepo(String repoName) {
        return gitHubEventMapper.toResponseDtos(gitHubEventRepository.findByRepoName(repoName));
    }

    @Override
    public List<GitHubEventResponseDto> getEventsByType(String type) {
        return gitHubEventMapper.toResponseDtos(gitHubEventRepository.findByType(type));
    }

    @Override
    public List<GitHubEventResponseDto> getRecentEvents(int hours) {
        return gitHubEventMapper.toResponseDtos(
                gitHubEventRepository.findByCreatedAtAfter(Instant.now().minus(Duration.ofHours(hours)))
        );
    }

    @Override
    public List<GitHubEventResponseDto> getUnprocessedEvents() {
        return gitHubEventMapper.toResponseDtos(gitHubEventRepository.findByProcessed(false));
    }
}
