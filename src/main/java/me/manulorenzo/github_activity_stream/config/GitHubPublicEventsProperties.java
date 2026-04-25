package me.manulorenzo.github_activity_stream.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "github.public-events")
public class GitHubPublicEventsProperties {
  private boolean enabled = true;
  private String apiUrl = "https://api.github.com/events";
  private Duration pollInterval = Duration.ofSeconds(5);
  private String apiVersion = "2022-11-28";
  private String userAgent = "github-activity-stream";
  private String authToken;
}
