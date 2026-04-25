package me.manulorenzo.github_activity_stream.mapper;

import java.time.Instant;
import java.util.List;
import me.manulorenzo.github_activity_stream.domain.GitHubEvent;
import me.manulorenzo.github_activity_stream.dto.GitHubEventResponseDto;
import me.manulorenzo.github_activity_stream.entity.GitHubEventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GitHubEventMapper {
  GitHubEventResponseDto toResponseDto(GitHubEventEntity entity);

  List<GitHubEventResponseDto> toResponseDtos(List<GitHubEventEntity> entities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "repoName", source = "event.repo.name")
  @Mapping(target = "actorLogin", source = "event.actor.login")
  @Mapping(target = "payload", source = "payloadJson")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "processed", constant = "false")
  GitHubEventEntity toEntity(GitHubEvent event, String payloadJson, Instant createdAt);
}
