package io.github.semyonburlak.wrapper.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DikidiApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public DikidiApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
