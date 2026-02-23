package io.github.semyonburlak.dikidiapi.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecordDto(
        CompanyDto company,
        List<ServiceDto> services,
        List<MasterDto> masters,
        LocalDateTime time,
        LocalDateTime timeTo
) {
}
