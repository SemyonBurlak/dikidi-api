package io.github.semyonburlak.dikidiapi.client;

import io.github.semyonburlak.dikidiapi.exception.DikidiApiException;
import io.github.semyonburlak.dikidiapi.exception.DikidiException;
import io.github.semyonburlak.dikidiapi.exception.DikidiHttpException;
import lombok.AllArgsConstructor;
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


@AllArgsConstructor
public class ErrorParser {

    public static DikidiException parseHttpError(HttpClientErrorException e, ObjectMapper objectMapper) {
        try {
            JsonNode errorNode = objectMapper.readTree(e.getResponseBodyAsString())
                    .path("error");
            return new DikidiApiException(errorNode);
        } catch (JacksonException ex) {
            return new DikidiHttpException(e.getStatusCode(), e.getMessage());
        }
    }
}
