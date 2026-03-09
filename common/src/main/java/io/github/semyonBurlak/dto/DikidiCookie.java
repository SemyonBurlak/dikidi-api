package io.github.semyonburlak.dto;

import java.time.Instant;

public record DikidiCookie(String value, Instant expiresAt) {
}
