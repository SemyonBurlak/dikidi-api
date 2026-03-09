package io.github.semyonburlak.dto;

import java.util.List;

public record CategoryDto(long id, String name, List<ServiceDto> services) {
}
