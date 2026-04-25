package me.manulorenzo.github_activity_stream.domain;

import java.util.Map;
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
