package io.github.semyonburlak.wrapper.mapper;

import io.github.semyonburlak.dto.CategoryDto;
import io.github.semyonburlak.dto.MasterDto;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiCategory;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiDatesTrue;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiMaster;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiService;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiServicesData;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiSlotsData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BookingCatalogMapperTest {

    private BookingCatalogMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BookingCatalogMapper();
    }

    @Test
    void toCategoryDtoListNullDataReturnsEmptyList() {
        assertThat(mapper.toCategoryDtoList(null)).isEmpty();
    }

    @Test
    void toCategoryDtoListNullListReturnsEmptyList() {
        DikidiServicesData data = new DikidiServicesData(null);
        assertThat(mapper.toCategoryDtoList(data)).isEmpty();
    }

    @Test
    void toCategoryDtoListValidDataReturnsMappedList() {
        DikidiService service = new DikidiService(101L, "Cut");
        DikidiCategory category = new DikidiCategory(1L, "Hair", List.of(service));
        DikidiServicesData data = new DikidiServicesData(Map.of("1", category));

        List<CategoryDto> result = mapper.toCategoryDtoList(data);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Hair");
        assertThat(result.get(0).services()).hasSize(1);
        assertThat(result.get(0).services().get(0).id()).isEqualTo(101L);
        assertThat(result.get(0).services().get(0).name()).isEqualTo("Cut");
    }

    @Test
    void toLocalDateListNullDataReturnsEmptyList() {
        assertThat(mapper.toLocalDateList(null)).isEmpty();
    }

    @Test
    void toLocalDateListNullDatesTrueReturnsEmptyList() {
        DikidiDatesTrue data = new DikidiDatesTrue(null);
        assertThat(mapper.toLocalDateList(data)).isEmpty();
    }

    @Test
    void toLocalDateListValidDataReturnsLocalDates() {
        DikidiDatesTrue data = new DikidiDatesTrue(List.of("2024-01-15", "2024-02-20"));
        List<LocalDate> result = mapper.toLocalDateList(data);
        assertThat(result).containsExactly(
                LocalDate.of(2024, 1, 15),
                LocalDate.of(2024, 2, 20));
    }

    @Test
    void toTimeMapNullDataReturnsEmptyMap() {
        assertThat(mapper.toTimeMap(null)).isEmpty();
    }

    @Test
    void toTimeMapValidDataMergesMastersPerSlot() {
        DikidiMaster master1 = new DikidiMaster(1L, "Alice");
        DikidiMaster master2 = new DikidiMaster(2L, "Bob");
        LocalDateTime slot = LocalDateTime.of(2024, 1, 15, 10, 0);
        Map<String, DikidiMaster> masters = Map.of("1", master1, "2", master2);
        Map<String, List<String>> times = Map.of(
                "1", List.of("2024-01-15 10:00:00"),
                "2", List.of("2024-01-15 10:00:00"));
        DikidiSlotsData data = new DikidiSlotsData(masters, times);

        Map<LocalDateTime, List<MasterDto>> result = mapper.toTimeMap(data);

        assertThat(result).hasSize(1);
        assertThat(result.get(slot)).hasSize(2);
        assertThat(result.get(slot).stream().map(MasterDto::username))
                .containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    void toTimeMapMasterIdNotInMastersSlotSkippedWithoutNpe() {
        DikidiMaster master = new DikidiMaster(1L, "Alice");
        LocalDateTime slot = LocalDateTime.of(2024, 1, 15, 10, 0);
        Map<String, DikidiMaster> masters = Map.of("1", master);
        Map<String, List<String>> times = Map.of("999", List.of("2024-01-15 10:00:00"));
        DikidiSlotsData data = new DikidiSlotsData(masters, times);

        Map<LocalDateTime, List<MasterDto>> result = mapper.toTimeMap(data);

        assertThat(result).isEmpty();
    }
}
