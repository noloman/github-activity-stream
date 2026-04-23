package me.manulorenzo.github_activity_stream.service;

import me.manulorenzo.github_activity_stream.domain.GitHubEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class GitHubPublicEventsService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RestTemplate restTemplate;

    public GitHubPublicEventsService(KafkaTemplate<String, String> kafkaTemplate, RestTemplate restTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 5000) // Poll every 5 seconds
    public void fetchPublicEvents() {
        try {
            GitHubEvent[] events = restTemplate.getForObject(
                    "https://api.github.com/events",
                    GitHubEvent[].class
            );
            if (events != null) {
                Arrays.stream(events).forEach(event -> {
                    try {
                        String eventJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(event);
                        kafkaTemplate.send("github-public-events", event.getType(), eventJson);
                    } catch (Exception e) {
                        System.err.println("Error serializing event: " + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error fetching public events: " + e.getMessage());
        }
    }
}
