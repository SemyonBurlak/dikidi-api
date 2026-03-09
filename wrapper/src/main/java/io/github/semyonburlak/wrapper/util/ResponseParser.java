package io.github.semyonburlak.wrapper.util;

import io.github.semyonburlak.wrapper.dto.DikidiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class ResponseParser {

    private final ObjectMapper objectMapper;

    public ResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> DikidiResult<T> parse(JsonNode root, Class<T> dataType) {

        if (root == null) {
            return DikidiResult.fail("NULL_ROOT", "Root is null");
        }

        JsonNode errorNode = root.path("error");

        if (errorNode.isMissingNode()) {
            return extractData(root, dataType);
        }

        if (errorNode.isValueNode()) {
            if (errorNode.isBoolean()) {
                if (!errorNode.booleanValue()) {
                    return extractData(root, dataType);
                }
                JsonNode messageNode = root.path("message");
                String messageValue = messageNode.isMissingNode() ? "No message" : messageNode.stringValue();
                return DikidiResult.fail("1", messageValue);
            }
            String errorValue = errorNode.isString() ? errorNode.stringValue() : String.valueOf(errorNode.asInt());
            JsonNode messageNode = root.path("message");
            String messageValue = messageNode == null ? "No message" : messageNode.stringValue();
            if (!errorValue.equals("0")) {
                return DikidiResult.fail(errorValue, messageValue);
            }
            return extractData(root, dataType);
        }

        if (errorNode.isObject()) {
            JsonNode codeNode = errorNode.path("code");

            boolean isSuccess =
                    codeNode.isMissingNode() || (codeNode.isNumber() && codeNode.asInt() == 0);

            if (!isSuccess) {
                String code = codeNode.isNumber()
                        ? String.valueOf(codeNode.asInt())
                        : codeNode.asString();
                return DikidiResult.fail(code, errorNode.path("message").asString("Unknown error"));
            }
            return extractData(root.path("data"), dataType);
        }

        String errorCode = errorNode.asString();
        return DikidiResult.fail(errorCode, errorCode);
    }

    private <T> DikidiResult<T> extractData(JsonNode node, Class<T> dataType) {
        try {
            T data = objectMapper.treeToValue(node, dataType);
            return DikidiResult.ok(data);
        } catch (JacksonException e) {
            return DikidiResult.fail("DATA_PARSE_ERROR", e.getMessage());
        } catch (Exception e) {
            return DikidiResult.fail("UNEXPECTED_ERROR", e.getMessage());
        }
    }
}
