package me.manulorenzo.github_activity_stream.mapper;

import me.manulorenzo.github_activity_stream.domain.GitHubEvent;
import me.manulorenzo.github_activity_stream.dto.GitHubEventResponseDto;
import me.manulorenzo.github_activity_stream.entity.GitHubEventEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubEventMapperTest {

    private final GitHubEventMapper mapper = Mappers.getMapper(GitHubEventMapper.class);

    @Test
    void toEntityMapsGithubEventToEntity() {

        GitHubEvent.Repo repo = new GitHubEvent.Repo();
        repo.setName("repoName");

        GitHubEvent.Actor actor = new GitHubEvent.Actor();
        actor.setLogin("actorLogin");

        GitHubEvent event = new GitHubEvent();
        event.setType("PushEvent");
        event.setRepo(repo);
        event.setActor(actor);
        event.setPayload(Map.of("ref", "refs/head/main"));

        String payloadJson = "{\"ref\":\"refs/head/main\"}";
        Instant createdAt = Instant.parse("2023-01-01T00:00:00Z");

        GitHubEventEntity entity = mapper.toEntity(event, payloadJson, createdAt);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getType()).isEqualTo("PushEvent");
        assertThat(entity.getRepoName()).isEqualTo("repoName");
        assertThat(entity.getActorLogin()).isEqualTo("actorLogin");
        assertThat(entity.getPayload()).isEqualTo(payloadJson);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.isProcessed()).isFalse();
    }

    @Test
    void toResponseDtoMapsEntityToDto() {
        GitHubEventEntity entity = new GitHubEventEntity();
        entity.setId(1L);
        entity.setType("PushEvent");
        entity.setRepoName("repoName");
        entity.setActorLogin("actorLogin");
        entity.setPayload("{\"ref\":\"refs/head/main\"}");
        Instant createdAt = Instant.parse("2023-01-01T00:00:00Z");
        entity.setCreatedAt(createdAt);
        entity.setProcessed(true);

        GitHubEventResponseDto dto = mapper.toResponseDto(entity);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getType()).isEqualTo("PushEvent");
        assertThat(dto.getRepoName()).isEqualTo("repoName");
        assertThat(dto.getActorLogin()).isEqualTo("actorLogin");
        assertThat(dto.getPayload()).isEqualTo("{\"ref\":\"refs/head/main\"}");
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dto.isProcessed()).isTrue();
    }

    @Test
    void toResponseDtosMapsEntityListToDtoList() {
        GitHubEventEntity entity = new GitHubEventEntity();
        entity.setId(1L);
        entity.setType("PushEvent");
        entity.setRepoName("repoName1");
        entity.setActorLogin("actorLogin1");
        entity.setPayload("{\"ref\":\"refs/head/main\"}");
        Instant createdAt1 = Instant.parse("2023-01-01T00:00:00Z");
        entity.setCreatedAt(createdAt1);
        entity.setProcessed(false);

        GitHubEventEntity entity2 = new GitHubEventEntity();
        entity2.setId(2L);
        entity2.setType("PullRequestEvent");
        entity2.setRepoName("repoName2");
        entity2.setActorLogin("actorLogin2");
        entity2.setPayload("{\"action\":\"opened\"}");
        Instant createdAt2 = Instant.parse("2023-01-02T00:00:00Z");
        entity2.setCreatedAt(createdAt2);
        entity2.setProcessed(true);

        List<GitHubEventResponseDto> dtos = mapper.toResponseDtos(List.of(entity, entity2));

        assertThat(dtos).hasSize(2);
        GitHubEventResponseDto gitHubEventResponseDto = dtos.get(0);
        assertThat(gitHubEventResponseDto.getId()).isEqualTo(1L);
        assertThat(gitHubEventResponseDto.getType()).isEqualTo("PushEvent");
        assertThat(gitHubEventResponseDto.getRepoName()).isEqualTo("repoName1");
        assertThat(gitHubEventResponseDto.getActorLogin()).isEqualTo("actorLogin1");
        assertThat(gitHubEventResponseDto.getPayload()).isEqualTo("{\"ref\":\"refs/head/main\"}");
        assertThat(gitHubEventResponseDto.getCreatedAt()).isEqualTo(createdAt1);
        assertThat(gitHubEventResponseDto.isProcessed()).isFalse();

        GitHubEventResponseDto gitHubEventResponseDto1 = dtos.get(1);
        assertThat(gitHubEventResponseDto1.getId()).isEqualTo(2L);
        assertThat(gitHubEventResponseDto1.getType()).isEqualTo("PullRequestEvent");
        assertThat(gitHubEventResponseDto1.getRepoName()).isEqualTo("repoName2");
        assertThat(gitHubEventResponseDto1.getActorLogin()).isEqualTo("actorLogin2");
        assertThat(gitHubEventResponseDto1.getPayload()).isEqualTo("{\"action\":\"opened\"}");
        assertThat(gitHubEventResponseDto1.getCreatedAt()).isEqualTo(createdAt2);
        assertThat(gitHubEventResponseDto1.isProcessed()).isTrue();

    }
}
