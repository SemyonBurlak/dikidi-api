package io.github.semyonburlak.dikidiapi.dto.dikidi;

import java.util.List;

public record DikidiCategory(long id, String name, List<DikidiService> services) {
}
