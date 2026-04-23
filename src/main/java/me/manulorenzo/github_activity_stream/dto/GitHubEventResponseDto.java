package me.manulorenzo.github_activity_stream.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GitHubEventResponseDto {
    private Long id;
    private String type;
    private String repoName;
    private String actorLogin;
    private Instant createdAt;
    private String payload;
    private boolean processed;
}
