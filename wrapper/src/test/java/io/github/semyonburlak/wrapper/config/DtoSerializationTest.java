package io.github.semyonburlak.wrapper.config;

import io.github.semyonburlak.dto.CompanyDto;
import io.github.semyonburlak.dto.RecordDto;
import io.github.semyonburlak.dto.ServiceDto;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiDatesTrue;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiRecord;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiServicesData;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiSlotsData;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiTimeReservation;
import io.github.semyonburlak.wrapper.support.WrapperTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class DtoSerializationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = WrapperTestConfig.buildObjectMapper();
    }

    @Test
    void dikidiTimeReservationDeserializesFromSnakeCase() throws Exception {
        String json = "{\"record_id\":42,\"master_id\":7,\"duration_string\":\"1:00\"}";
        DikidiTimeReservation dto = mapper.readValue(json, DikidiTimeReservation.class);
        assertThat(dto.recordId()).isEqualTo(42);
        assertThat(dto.masterId()).isEqualTo(7);
        assertThat(dto.durationString()).isEqualTo("1:00");
    }

    @Test
    void dikidiTimeReservationSerializesToSnakeCase() throws Exception {
        DikidiTimeReservation dto = new DikidiTimeReservation(10L, 5L, "2:00");
        JsonNode node = mapper.valueToTree(dto);
        assertThat(node.has("record_id")).isTrue();
        assertThat(node.has("master_id")).isTrue();
        assertThat(node.has("duration_string")).isTrue();
        assertThat(node.get("record_id").asLong()).isEqualTo(10L);
    }

    @Test
    void dikidiServicesDataDeserializesNestedStructure() throws Exception {
        String json = "{\"list\": {\"1\": {\"id\": 1, \"name\": \"Hair\","
                + " \"services\": [{\"id\": 101, \"name\": \"Cut\"}]}}}";
        DikidiServicesData dto = mapper.readValue(json, DikidiServicesData.class);
        assertThat(dto.list()).hasSize(1);
        assertThat(dto.list().get("1").id()).isEqualTo(1);
        assertThat(dto.list().get("1").name()).isEqualTo("Hair");
        assertThat(dto.list().get("1").services()).hasSize(1);
        assertThat(dto.list().get("1").services().get(0).name()).isEqualTo("Cut");
    }

    @Test
    void dikidiSlotsDataDeserializesTimesAsStrings() throws Exception {
        String json = "{\"masters\": {\"1\": {\"id\": 1, \"username\": \"John\"}},"
                + " \"times\": {\"1\": [\"2024-01-15 10:00:00\"]}}";
        DikidiSlotsData dto = mapper.readValue(json, DikidiSlotsData.class);
        assertThat(dto.masters()).hasSize(1);
        assertThat(dto.times().get("1")).hasSize(1);
        assertThat(dto.times().get("1").get(0)).isEqualTo("2024-01-15 10:00:00");
    }

    @Test
    void dikidiDatesTrueDeserializesWithSnakeCaseNaming() throws Exception {
        // @JsonProperty("dates_true") from com.fasterxml.jackson is not processed by Jackson 3.x.
        // PropertyNamingStrategies.SNAKE_CASE maps datesTrue -> dates_true automatically.
        String json = "{\"dates_true\": [\"2024-01-15\", \"2024-01-16\"]}";
        DikidiDatesTrue dto = mapper.readValue(json, DikidiDatesTrue.class);
        assertThat(dto.datesTrue()).containsExactly("2024-01-15", "2024-01-16");
    }

    @Test
    void localDateSerializesToYyyyMmDd() throws Exception {
        LocalDate date = LocalDate.of(2024, 1, 15);
        String json = mapper.writeValueAsString(date);
        assertThat(json).isEqualTo("\"2024-01-15\"");
    }

    @Test
    void localDateTimeSerializesToYyyyMmDdHhMmSs() throws Exception {
        LocalDateTime dt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        String json = mapper.writeValueAsString(dt);
        assertThat(json).isEqualTo("\"2024-01-15 10:30:00\"");
    }

    @Test
    void dikidiRecordDeserializesSpaceSeparatedDateTime() throws Exception {
        String json = "{\"id\":1,\"time\":\"2026-03-11 14:15:00\",\"time_to\":\"2026-03-11 15:00:00\","
                + "\"master_username\":\"john\",\"master_post\":\"stylist\","
                + "\"service_id\":42,\"service_name\":\"Haircut\"}";
        DikidiRecord record = mapper.readValue(json, DikidiRecord.class);
        assertThat(record.time()).isEqualTo(LocalDateTime.of(2026, 3, 11, 14, 15, 0));
        assertThat(record.timeTo()).isEqualTo(LocalDateTime.of(2026, 3, 11, 15, 0, 0));
    }

    @Test
    void recordDtoLocalDateTimeSerializedByCustomSerializer() throws Exception {
        // @JsonFormat from com.fasterxml.jackson.annotation is ignored by Jackson 3.x.
        // The custom LocalDateTimeSerializer (yyyy-MM-dd HH:mm:ss) is used instead.
        LocalDateTime time = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        RecordDto dto = new RecordDto(
                1L, time, time, new CompanyDto(1L, "Salon"),
                List.of(new ServiceDto(1L, "Cut")), List.of());
        JsonNode node = mapper.valueToTree(dto);
        assertThat(node.get("time").asString()).isEqualTo("2024-01-15 10:00:00");
        assertThat(node.get("time_to").asString()).isEqualTo("2024-01-15 10:00:00");
    }

    @Test
    void failOnUnknownPropertiesDisabledNoExceptionOnUnknownField() {
        String json = "{\"record_id\":1,\"master_id\":2,\"duration_string\":\"1:00\","
                + "\"unknown_field\":\"ignored\"}";
        assertThatNoException().isThrownBy(
                () -> mapper.readValue(json, DikidiTimeReservation.class));
    }
}
