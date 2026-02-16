package io.github.semyonburlak.dikidiapi.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;

@Getter
@AllArgsConstructor
public enum DikidiErrorCode {

    // Auth
    USER_LOGGED( "User is already authorized", HttpStatus.CONFLICT),
    USER_AUTHORISATION_ERROR("You must enter username and password to login", HttpStatus.BAD_REQUEST),
    NUMBER_NOT_TRUE("Probably you entered incorrect number", HttpStatus.BAD_REQUEST),
    USER_AUTHORISATION_DATA_ERROR("Wrong login or password", HttpStatus.UNAUTHORIZED),


    EMPTY_RESPONSE("Unexpected empty response", HttpStatus.BAD_GATEWAY),




    UNKNOWN("UNKNOWN", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;

    public static DikidiErrorCode fromString(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    public static DikidiErrorCode fromJsonNode(JsonNode codeNode) {
        String raw = codeNode.isNumber()
                ? String.valueOf(codeNode.asInt())
                : codeNode.asString();
        return fromString(raw);
    }
}