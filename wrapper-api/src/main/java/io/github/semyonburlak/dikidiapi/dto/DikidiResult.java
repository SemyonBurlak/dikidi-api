package io.github.semyonburlak.dikidiapi.dto;

public record DikidiResult<T>(boolean success, T data, DikidiError error) {
    public static <T> DikidiResult<T> ok(T data) {
        return new DikidiResult<>(true, data, null);
    }

    public static <T> DikidiResult<T> fail(Object code, String message) {
        return new DikidiResult<>(false, null, new DikidiError(code, message));
    }
}
