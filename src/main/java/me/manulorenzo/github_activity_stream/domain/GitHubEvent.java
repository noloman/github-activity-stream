package me.manulorenzo.github_activity_stream.domain;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GitHubEvent {
  private String type;
  private Repo repo;
  private Map<String, Object> payload;
  private Actor actor;
  private String Id;

  @JsonProperty("created_at")
  private Instant createdAt;

  @Getter
  @Setter
  public static class Repo {
    private String name;
  }

  @Getter
  @Setter
  public static class Actor {
    private String login;
  }
}
