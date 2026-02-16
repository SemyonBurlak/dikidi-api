package io.github.semyonburlak.dikidiapi.exception;

public class DikidiException extends RuntimeException {

    public DikidiException(String message) {
        super(message);
    }

    public DikidiException(String message, Throwable cause) {
        super(message, cause);
    }
}
