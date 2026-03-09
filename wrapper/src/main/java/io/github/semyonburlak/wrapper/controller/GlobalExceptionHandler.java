package io.github.semyonburlak.wrapper.controller;

import io.github.semyonburlak.wrapper.exception.DikidiApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DikidiApiException.class)
    ResponseEntity<Map<String, String>> handle(DikidiApiException e) {
        return ResponseEntity.status(e.getStatus())
                .body(Map.of(
                                "code", e.getCode(),
                                "message", e.getMessage()
                        )
                );
    }
}
