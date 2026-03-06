package io.github.semyonburlak.dikidiapi.dto;

import jakarta.annotation.Nullable;

public record DikidiError(String code, @Nullable String message) {
}
