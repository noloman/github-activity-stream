package me.manulorenzo.github_activity_stream.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record ErrorResponseDto(
    Instant timestamp, int status, String error, String message, String path) {}
