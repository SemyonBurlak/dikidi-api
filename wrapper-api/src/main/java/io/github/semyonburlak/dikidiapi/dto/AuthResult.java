package io.github.semyonburlak.dikidiapi.dto;

import java.time.Instant;

public record AuthResult(String sessionId, String token, Instant expiresAt) {
}
