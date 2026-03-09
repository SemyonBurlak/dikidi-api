package io.github.semyonburlak.wrapper.dto;

import jakarta.annotation.Nullable;

public record DikidiError(String code, @Nullable String message) {
}
