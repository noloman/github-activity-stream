package me.manulorenzo.github_activity_stream.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "github_events")
public class GitHubEventEntity {
  // Getters and setters
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "repo_name", nullable = false)
  private String repoName;

  @Column(name = "actor_login", nullable = false)
  private String actorLogin;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "processed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean processed = false;
}
