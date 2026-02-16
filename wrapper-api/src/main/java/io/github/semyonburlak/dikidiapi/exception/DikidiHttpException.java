package io.github.semyonburlak.dikidiapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class DikidiHttpException extends DikidiException {

    private final HttpStatusCode statusCode;

    public DikidiHttpException(HttpStatusCode statusCode, String message) {
        super(message);

        this.statusCode = statusCode;
    }
}
