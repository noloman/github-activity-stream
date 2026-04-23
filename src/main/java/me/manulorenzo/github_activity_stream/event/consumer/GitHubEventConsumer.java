package me.manulorenzo.github_activity_stream.event.consumer;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GitHubEventConsumer {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public GitHubEventConsumer(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @KafkaListener(topics = "github-public-events")
    public void consume(String eventJson) {
        simpMessagingTemplate.convertAndSend("/topic/github-events", eventJson);
    }
}
