package me.manulorenzo.github_activity_stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GithubActivityStreamApplication {
    public static void main(String[] args) {
        SpringApplication.run(GithubActivityStreamApplication.class, args);
    }
}
