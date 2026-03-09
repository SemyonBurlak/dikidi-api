package io.github.semyonburlak.wrapper.dto;

import io.github.semyonburlak.wrapper.exception.DikidiApiException;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@Slf4j
public record DikidiResponse<T>(
        DikidiResult<T> result,

        @Nullable List<String> cookies,
        @Nullable HttpHeaders headers
) {
    private static final Map<String, HttpStatus> INFRA_ERRORS = Map.of(
            "RATE_LIMITED", HttpStatus.TOO_MANY_REQUESTS,
            "NETWORK", HttpStatus.SERVICE_UNAVAILABLE,
            "NULL_ROOT", HttpStatus.BAD_GATEWAY,
            "DATA_PARSE_ERROR", HttpStatus.BAD_GATEWAY
    );

    public static <T> DikidiResponse<T> of(DikidiResult<T> result, @Nullable HttpHeaders headers) {
        if (headers == null) {
            return new DikidiResponse<>(result, null, null);
        }
        List<String> setCookie = headers.get(HttpHeaders.SET_COOKIE);

        return new DikidiResponse<>(result, setCookie, headers);
    }

    public T resolve(Map<String, HttpStatus> errorMap) {
        if (result.success()) {
            return result.data();
        }
        DikidiError error = result.error();
        String code;
        if (error == null || error.code() == null) {
            log.warn("Error or code is null: {}", result);
            code = "ERROR_IS_NULL";
        } else {
            code = error.code();
        }

        String message = error != null ? error.message() : "Unknown error";
        HttpStatus status = errorMap.containsKey(code)
                ? errorMap.get(code)
                : INFRA_ERRORS.getOrDefault(code, HttpStatus.BAD_GATEWAY);
        throw new DikidiApiException(status, code, message);
    }

    public T resolve() {
        return resolve(Map.of());
    }
}
