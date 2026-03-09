package io.github.semyonburlak.wrapper.dto.dikidi;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@ToString
public class DikidiRecordsData {

    private final Map<String, DikidiRecordsInfo> entries = new LinkedHashMap<>();

    @JsonAnySetter
    public void addEntry(String id, DikidiRecordsInfo entry) {
        entries.put(id, entry);
    }
}
