package io.github.semyonburlak.wrapper.dto.dikidi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DikidiRecord(
        Long id,
        String time,
        @JsonProperty("time_to") String timeTo,
        @JsonProperty("master_username") String masterUsername,
        @JsonProperty("master_post") String masterPost,
        @JsonProperty("service_id") String serviceId,
        @JsonProperty("service_name") String serviceName
) {

}
