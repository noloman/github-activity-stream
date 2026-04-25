package me.manulorenzo.github_activity_stream.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import me.manulorenzo.github_activity_stream.dto.GitHubEventResponseDto;
import me.manulorenzo.github_activity_stream.service.GitHubEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GitHubEventController.class)
class GitHubEventControllerTest {
  @MockitoBean private GitHubEventService gitHubEventService;

  @Autowired private MockMvc mockMvc;

  @Test
  void getAllEventsReturnsOkWithExpectedPageShape() throws Exception {
    when(gitHubEventService.getAllEvents(0, 10))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

    mockMvc
        .perform(get("/api/v1/events").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.totalPages").value(0))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.number").value(0))
        .andExpect(jsonPath("$.first").value(true))
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  void getAllEventsReturnsOkWithEventContent() throws Exception {
    GitHubEventResponseDto event =
        new GitHubEventResponseDto(
            1L,
            "PushEvent",
            "owner/repo",
            "manu",
            Instant.parse("2026-04-25T08:00:00Z"),
            "{\"key\":\"value\"}",
            false);

    when(gitHubEventService.getAllEvents(0, 10))
        .thenReturn(new PageImpl<>(List.of(event), PageRequest.of(0, 10), 1));

    mockMvc
        .perform(get("/api/v1/events").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].type").value("PushEvent"))
        .andExpect(jsonPath("$.content[0].repoName").value("owner/repo"))
        .andExpect(jsonPath("$.content[0].actorLogin").value("manu"))
        .andExpect(jsonPath("$.content[0].processed").value(false))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.totalPages").value(1));
  }

  @Test
  void getAllEventsReturnsBadRequestWhenPageIsNegative() throws Exception {
    mockMvc
        .perform(get("/api/v1/events").param("page", "-1").param("size", "10"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value(containsString("page")))
        .andExpect(jsonPath("$.path").value("/api/v1/events"));
  }

  @Test
  void getRecentEventsReturnsBadRequestWhenHoursIsNotANumber() throws Exception {
    mockMvc
        .perform(get("/api/v1/events/recent").param("hours", "abc"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value(containsString("hours")))
        .andExpect(jsonPath("$.path").value("/api/v1/events/recent"));
  }

  @Test
  void getRecentEventsReturnsBadRequestWhenHoursIsNegative() throws Exception {
    mockMvc
        .perform(get("/api/v1/events/recent").param("hours", "-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value(containsString("hours")))
        .andExpect(jsonPath("$.path").value("/api/v1/events/recent"));
  }

  @Test
  void getEventsByRepoReturnsOk() throws Exception {
    when(gitHubEventService.getEventsByRepo("repo")).thenReturn(List.of());
    mockMvc
        .perform(get("/api/v1/events/repo").param("repoName", "repo"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void getEventsByRepoReturnsBadRequestWhenRepoNameIsBlank() throws Exception {
    mockMvc
        .perform(get("/api/v1/events/repo").param("repoName", " "))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value(containsString("repoName")))
        .andExpect(jsonPath("$.path").value("/api/v1/events/repo"));
  }

  @Test
  void getEventsByTypeReturnsOk() throws Exception {
    when(gitHubEventService.getEventsByType("type")).thenReturn(List.of());
    mockMvc
        .perform(get("/api/v1/events/type").param("type", "type"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void getEventsByTypeReturnsBadRequestWhenTypeIsBlank() throws Exception {
    mockMvc
        .perform(get("/api/v1/events/type").param("type", " "))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value(containsString("type")))
        .andExpect(jsonPath("$.path").value("/api/v1/events/type"));
  }

  @Test
  void getUnprocessedEventsReturnsOk() throws Exception {
    when(gitHubEventService.getUnprocessedEvents()).thenReturn(List.of());
    mockMvc
        .perform(get("/api/v1/events/unprocessed"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }
}
