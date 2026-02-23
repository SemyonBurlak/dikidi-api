package io.github.semyonburlak.dikidiapi.util;

import io.github.semyonburlak.dikidiapi.dto.DikidiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ResponseParser {
    private final ObjectMapper objectMapper;

    public <T> DikidiResult<T> parse(JsonNode root, Class<T> dataType) {

        if (root == null) {
            return DikidiResult.fail("NULL_ROOT", "Root is null");
        }

        JsonNode errorNode = root.path("error");

        if (errorNode.isObject()) {
            JsonNode codeNode = errorNode.path("code");

            boolean isSuccess = codeNode.isMissingNode() || (codeNode.isNumber() && codeNode.asInt() == 0);

            if (!isSuccess) {
                return DikidiResult.fail(codeNode.isString() ? codeNode.toString() : codeNode.asInt(),
                        errorNode.path("message").asString("Unknown error"));
            }
            return extractData(root.path("data"), dataType);
        }

        if (errorNode.isMissingNode()) {
            return extractData(root, dataType);
        }

        return DikidiResult.fail("UNKNOWN", errorNode.asString());
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
