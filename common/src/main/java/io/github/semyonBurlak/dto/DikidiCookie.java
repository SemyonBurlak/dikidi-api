package io.github.semyonBurlak.dto;

import java.time.Instant;

public record DikidiCookie(String value, Instant expiresAt) {
}
