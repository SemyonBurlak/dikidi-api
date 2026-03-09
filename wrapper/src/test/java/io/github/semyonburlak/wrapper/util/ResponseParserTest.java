package io.github.semyonburlak.wrapper.util;

import io.github.semyonburlak.wrapper.dto.DikidiResult;
import io.github.semyonburlak.wrapper.support.WrapperTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseParserTest {

    private ResponseParser parser;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = WrapperTestConfig.buildObjectMapper();
        parser = new ResponseParser(mapper);
    }

    private JsonNode json(String jsonStr) throws Exception {
        return mapper.readTree(jsonStr);
    }

    @Test
    void format1StringErrorReturnsFail() throws Exception {
        DikidiResult<String> result = parser.parse(
                json("{\"error\": \"COMPANY_ERROR\", \"message\": \"not found\"}"),
                String.class);
        assertThat(result.success()).isFalse();
        assertThat(result.error().code()).isEqualTo("COMPANY_ERROR");
        assertThat(result.error().message()).isEqualTo("not found");
    }

    @Test
    void format1IntErrorCodeConvertedToString() throws Exception {
        DikidiResult<String> result = parser.parse(
                json("{\"error\": 1, \"message\": \"err\"}"),
                String.class);
        assertThat(result.success()).isFalse();
        assertThat(result.error().code()).isEqualTo("1");
    }

    @Test
    void format1IntErrorZeroReturnsSuccess() throws Exception {
        DikidiResult<JsonNode> result = parser.parse(
                json("{\"error\": 0, \"message\": \"ok\"}"),
                JsonNode.class);
        assertThat(result.success()).isTrue();
    }

    @Test
    void format1StringErrorZeroReturnsSuccess() throws Exception {
        DikidiResult<JsonNode> result = parser.parse(
                json("{\"error\": \"0\", \"message\": \"ok\"}"),
                JsonNode.class);
        assertThat(result.success()).isTrue();
    }

    @Test
    void format1BooleanFalseReturnsSuccess() throws Exception {
        // error=false means no error (same as error=0)
        DikidiResult<JsonNode> result = parser.parse(
                json("{\"error\": false, \"message\": \"ok\"}"),
                JsonNode.class);
        assertThat(result.success()).isTrue();
    }

    @Test
    void format1BooleanTrueReturnsFail() throws Exception {
        DikidiResult<JsonNode> result = parser.parse(
                json("{\"error\": true, \"message\": \"fail\"}"),
                JsonNode.class);
        assertThat(result.success()).isFalse();
        assertThat(result.error().code()).isEqualTo("1");
        assertThat(result.error().message()).isEqualTo("fail");
    }

    @Test
    void format2ErrorObjectWithStringCodeReturnsFail() throws Exception {
        String jsonStr = "{\"error\": {\"code\": \"USER_AUTHORISATION_ERROR\", \"message\": \"bad auth\"}}";
        DikidiResult<String> result = parser.parse(json(jsonStr), String.class);
        assertThat(result.success()).isFalse();
        assertThat(result.error().code()).isEqualTo("USER_AUTHORISATION_ERROR");
        assertThat(result.error().message()).isEqualTo("bad auth");
    }

    @Test
    void format2ErrorObjectWithNumericCodeConvertedToString() throws Exception {
        String jsonStr = "{\"error\": {\"code\": 400, \"message\": \"bad request\"}, \"data\": null}";
        DikidiResult<String> result = parser.parse(json(jsonStr), String.class);
        assertThat(result.success()).isFalse();
        assertThat(result.error().code()).isEqualTo("400");
    }

    @Test
    void format2ErrorObjectCodeZeroReturnsSuccess() throws Exception {
        String jsonStr = "{\"error\": {\"code\": 0}, \"data\": {\"list\": {}}}";
        DikidiResult<JsonNode> result = parser.parse(json(jsonStr), JsonNode.class);
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isNotNull();
    }

    @Test
    void format2ErrorObjectMissingCodeReturnsSuccess() throws Exception {
        String jsonStr = "{\"error\": {}, \"data\": {\"list\": {}}}";
        DikidiResult<JsonNode> result = parser.parse(json(jsonStr), JsonNode.class);
        assertThat(result.success()).isTrue();
    }

    @Test
    void format3NoErrorFieldReturnsSuccessWithRoot() throws Exception {
        DikidiResult<JsonNode> result = parser.parse(json("{\"list\": {}}"), JsonNode.class);
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isNotNull();
    }

    @Test
    void nullRootReturnsNullRootFail() {
        DikidiResult<String> result = parser.parse(null, String.class);
        assertThat(result.success()).isFalse();
        assertThat(result.error().code()).isEqualTo("NULL_ROOT");
    }

    @Test
    void incompatibleJsonTypeReturnsDataParseError() throws Exception {
        // Array node cannot be deserialized into a record — triggers DATA_PARSE_ERROR
        JsonNode arrayNode = mapper.readTree("[1, 2, 3]");
        DikidiResult<StrictDto> result = parser.parse(arrayNode, StrictDto.class);
        assertThat(result.success()).isFalse();
        assertThat(result.error().code()).isEqualTo("DATA_PARSE_ERROR");
    }

    record StrictDto(String name, int value) {
    }
}
