package io.github.semyonburlak.dto;

import java.util.List;

public record AppointmentPageDto(boolean more, List<RecordDto> list) {
}
