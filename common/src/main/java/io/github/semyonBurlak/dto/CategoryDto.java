package io.github.semyonBurlak.dto;

import java.util.List;

public record CategoryDto(long id, String name, List<ServiceDto> services) {
}
