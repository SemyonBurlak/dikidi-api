package io.github.semyonburlak.wrapper.dto.dikidi;

import java.util.List;
import java.util.Map;

public record DikidiServicesData(Map<String, DikidiCategory> list) {
    public List<DikidiCategory> categories() {
        return list == null ? java.util.List.of() : java.util.List.copyOf(list.values());
    }
}
