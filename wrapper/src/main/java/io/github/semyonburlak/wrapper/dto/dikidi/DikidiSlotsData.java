package io.github.semyonburlak.wrapper.dto.dikidi;

import java.util.List;
import java.util.Map;

public record DikidiSlotsData(Map<String, DikidiMaster> masters, Map<String, List<String>> times) {
}
