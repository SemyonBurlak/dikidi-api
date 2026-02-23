package io.github.semyonburlak.dikidiapi.dto;

import jakarta.annotation.Nullable;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.stream.Collectors;

public record DikidiResponse<T>(
        DikidiResult<T> result,
        @Nullable String cookies,
        HttpHeaders headers
) {
    public static <T> DikidiResponse<T> of(DikidiResult<T> result, HttpHeaders headers) {
        List<String> setCookies = headers.getValuesAsList(HttpHeaders.SET_COOKIE);
        String cookies = setCookies.isEmpty() ? null : setCookies.stream()
                .map(h -> h.split(";", 2)[0])
                .collect(Collectors.joining("; "));
        return new DikidiResponse<>(result, cookies, headers);
    }
}
