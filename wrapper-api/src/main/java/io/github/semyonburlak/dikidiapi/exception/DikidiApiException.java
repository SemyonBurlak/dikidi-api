package io.github.semyonburlak.dikidiapi.exception;

import lombok.Getter;
import tools.jackson.databind.JsonNode;

@Getter
public class DikidiApiException extends DikidiException {

    private final DikidiErrorCode dikidiErrorCode;

    public DikidiApiException(DikidiErrorCode dikidiErrorCode, String message) {
        super(message);
        this.dikidiErrorCode = dikidiErrorCode;
    }

    public DikidiApiException(JsonNode errorNode) {
        this(
                DikidiErrorCode.fromJsonNode(errorNode.path("code")),
                errorNode.path("message").asString()
        );
    }
}
