package me.manulorenzo.github_activity_stream.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
  @Bean
  RestTemplate restTemplate(GitHubPublicEventsProperties gitHubPublicEventsProperties) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(10_000);
    requestFactory.setReadTimeout(10_000);

    RestTemplate restTemplate = new RestTemplate(requestFactory);
    restTemplate
        .getInterceptors()
        .add(
            (request, body, execution) -> {
              HttpHeaders headers = request.getHeaders();
              headers.setAccept(List.of(MediaType.APPLICATION_JSON));
              headers.set("X-GitHub-Api-Version", gitHubPublicEventsProperties.getApiVersion());
              headers.set(HttpHeaders.USER_AGENT, gitHubPublicEventsProperties.getUserAgent());

              if (StringUtils.hasText(gitHubPublicEventsProperties.getAuthToken())) {
                headers.setBearerAuth(gitHubPublicEventsProperties.getAuthToken().trim());
              }

              return execution.execute(request, body);
            });
    return restTemplate;
  }
}
