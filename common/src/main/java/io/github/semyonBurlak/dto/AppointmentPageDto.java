package io.github.semyonBurlak.dto;

import java.util.List;

public record AppointmentPageDto(boolean more, List<RecordDto> list) {
}
