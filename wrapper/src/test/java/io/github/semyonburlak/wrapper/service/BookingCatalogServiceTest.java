package io.github.semyonburlak.wrapper.service;

import io.github.semyonburlak.dto.CategoryDto;
import io.github.semyonburlak.dto.MasterDto;
import io.github.semyonburlak.wrapper.client.DikidiHttpClient;
import io.github.semyonburlak.wrapper.dto.DikidiResponse;
import io.github.semyonburlak.wrapper.dto.DikidiResult;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiCategory;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiDatesTrue;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiMaster;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiService;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiServicesData;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiSlotsData;
import io.github.semyonburlak.wrapper.exception.DikidiApiException;
import io.github.semyonburlak.wrapper.mapper.BookingCatalogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class BookingCatalogServiceTest {

    @Mock
    private DikidiHttpClient dikidiHttpClient;

    private BookingCatalogService service;

    @BeforeEach
    void setUp() {
        service = new BookingCatalogService(dikidiHttpClient, new BookingCatalogMapper());
    }

    @Test
    void getCategoriesSuccessReturnsMappedList() {
        DikidiService svc = new DikidiService(101L, "Cut");
        DikidiCategory cat = new DikidiCategory(1L, "Hair", List.of(svc));
        DikidiServicesData data = new DikidiServicesData(Map.of("1", cat));
        doReturn(DikidiResponse.of(DikidiResult.ok(data), null))
                .when(dikidiHttpClient).get(any(), any(), eq(DikidiServicesData.class));

        List<CategoryDto> result = service.getCategories(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Hair");
    }

    @Test
    void getCategoriesCompanyErrorThrowsWith404() {
        doReturn(DikidiResponse.of(DikidiResult.fail("COMPANY_ERROR", "not found"), null))
                .when(dikidiHttpClient).get(any(), any(), eq(DikidiServicesData.class));

        assertThatThrownBy(() -> service.getCategories(1L))
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getDatesTrueSuccessReturnsLocalDates() {
        DikidiDatesTrue data = new DikidiDatesTrue(List.of("2024-01-15", "2024-01-16"));
        doReturn(DikidiResponse.of(DikidiResult.ok(data), null))
                .when(dikidiHttpClient).get(any(), any(), eq(DikidiDatesTrue.class));

        List<LocalDate> result = service.getDatesTrue(
                1L, 2L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

        assertThat(result).containsExactly(
                LocalDate.of(2024, 1, 15),
                LocalDate.of(2024, 1, 16));
    }

    @Test
    void getTimesSuccessMergesSlots() {
        DikidiDatesTrue dates = new DikidiDatesTrue(List.of("2024-01-15"));
        DikidiMaster master = new DikidiMaster(1L, "Alice");
        LocalDateTime slot = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        DikidiSlotsData slotsData = new DikidiSlotsData(
                Map.of("1", master),
                Map.of("1", List.of("2024-01-15 10:00:00")));

        doReturn(DikidiResponse.of(DikidiResult.ok(dates), null))
                .when(dikidiHttpClient).get(contains("get_dates_true"), any(), eq(DikidiDatesTrue.class));
        doReturn(DikidiResponse.of(DikidiResult.ok(slotsData), null))
                .when(dikidiHttpClient).get(contains("get_datetimes"), any(), eq(DikidiSlotsData.class));

        Map<LocalDateTime, List<MasterDto>> result = service.getTimes(
                1L, 2L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

        assertThat(result).containsKey(slot);
        assertThat(result.get(slot)).hasSize(1);
        assertThat(result.get(slot).get(0).username()).isEqualTo("Alice");
    }

    @Test
    void getTimesSlotErrorLogsAndReturnsEmptyMap() {
        DikidiDatesTrue dates = new DikidiDatesTrue(List.of("2024-01-15"));
        doReturn(DikidiResponse.of(DikidiResult.ok(dates), null))
                .when(dikidiHttpClient).get(contains("get_dates_true"), any(), eq(DikidiDatesTrue.class));
        doReturn(DikidiResponse.of(DikidiResult.fail("NETWORK", "error"), null))
                .when(dikidiHttpClient).get(contains("get_datetimes"), any(), eq(DikidiSlotsData.class));

        Map<LocalDateTime, List<MasterDto>> result = service.getTimes(
                1L, 2L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

        assertThat(result).isEmpty();
    }
}
