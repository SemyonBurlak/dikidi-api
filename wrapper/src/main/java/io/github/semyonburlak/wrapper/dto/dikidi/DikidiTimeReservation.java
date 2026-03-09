package io.github.semyonburlak.wrapper.dto.dikidi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DikidiTimeReservation(@JsonProperty("record_id") Long recordId,
                                    @JsonProperty("master_id") Long masterId,
                                    @JsonProperty("duration_string") String durationString
) {
}
