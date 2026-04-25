package me.manulorenzo.github_activity_stream.repository;

import java.time.Instant;
import java.util.List;
import me.manulorenzo.github_activity_stream.entity.GitHubEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GitHubEventRepository
    extends JpaRepository<GitHubEventEntity, Long>, JpaSpecificationExecutor<GitHubEventEntity> {
  List<GitHubEventEntity> findByRepoName(String name);

  List<GitHubEventEntity> findByType(String type);

  List<GitHubEventEntity> findByCreatedAtAfter(Instant since);

  List<GitHubEventEntity> findByProcessed(boolean processed);
}
