package io.github.semyonburlak.wrapper.dto;

import io.github.semyonburlak.wrapper.exception.DikidiApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DikidiResponseTest {

    @Test
    void resolveSuccessResultReturnsData() {
        DikidiResponse<String> response = DikidiResponse.of(DikidiResult.ok("hello"), null);
        assertThat(response.resolve()).isEqualTo("hello");
    }

    @Test
    void resolveErrorCodeInMapThrowsWithMappedStatus() {
        DikidiResponse<String> response = DikidiResponse.of(
                DikidiResult.fail("COMPANY_ERROR", "not found"), null);
        Map<String, HttpStatus> errorMap = Map.of("COMPANY_ERROR", HttpStatus.NOT_FOUND);
        assertThatThrownBy(() -> response.resolve(errorMap))
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> {
                    DikidiApiException ex = (DikidiApiException) e;
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getCode()).isEqualTo("COMPANY_ERROR");
                });
    }

    @Test
    void resolveRateLimitedCodeThrowsWith429() {
        DikidiResponse<String> response = DikidiResponse.of(
                DikidiResult.fail("RATE_LIMITED", "Too many requests"), null);
        assertThatThrownBy(response::resolve)
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }

    @Test
    void resolveNetworkCodeThrowsWith503() {
        DikidiResponse<String> response = DikidiResponse.of(
                DikidiResult.fail("NETWORK", "connection refused"), null);
        assertThatThrownBy(response::resolve)
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void resolveNullRootCodeThrowsWith502() {
        DikidiResponse<String> response = DikidiResponse.of(
                DikidiResult.fail("NULL_ROOT", "root is null"), null);
        assertThatThrownBy(response::resolve)
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void resolveDataParseErrorCodeThrowsWith502() {
        DikidiResponse<String> response = DikidiResponse.of(
                DikidiResult.fail("DATA_PARSE_ERROR", "parse failed"), null);
        assertThatThrownBy(response::resolve)
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void resolveUnknownCodeThrowsWith502() {
        DikidiResponse<String> response = DikidiResponse.of(
                DikidiResult.fail("UNKNOWN_CODE", "something"), null);
        assertThatThrownBy(response::resolve)
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void resolveNullErrorLogsAndThrowsWith502() {
        DikidiResult<String> failResult = new DikidiResult<>(false, null, null);
        DikidiResponse<String> response = DikidiResponse.of(failResult, null);
        assertThatThrownBy(response::resolve)
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> {
                    DikidiApiException ex = (DikidiApiException) e;
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(ex.getCode()).isEqualTo("ERROR_IS_NULL");
                });
    }
}
