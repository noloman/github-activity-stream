package me.manulorenzo.github_activity_stream.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ErrorResponseDto(Instant timestamp, int status, String error, String message, String path) {
}
