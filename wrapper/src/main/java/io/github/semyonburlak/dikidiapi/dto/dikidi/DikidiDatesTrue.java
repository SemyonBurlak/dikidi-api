package io.github.semyonburlak.dikidiapi.dto.dikidi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DikidiDatesTrue(@JsonProperty("dates_true") List<String> datesTrue) {
}
