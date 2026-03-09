package io.github.semyonburlak.wrapper.dto.dikidi;

import java.util.List;

public record DikidiCategory(Long id, String name, List<DikidiService> services) {
}
