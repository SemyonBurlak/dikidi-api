package io.github.semyonburlak.wrapper.service;

import io.github.semyonburlak.dto.CategoryDto;
import io.github.semyonburlak.dto.MasterDto;
import io.github.semyonburlak.wrapper.client.DikidiHttpClient;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiDatesTrue;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiServicesData;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiSlotsData;
import io.github.semyonburlak.wrapper.exception.DikidiApiException;
import io.github.semyonburlak.wrapper.mapper.BookingCatalogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class BookingCatalogService {

    private final DikidiHttpClient dikidiHttpClient;
    private final BookingCatalogMapper mapper;

    public BookingCatalogService(DikidiHttpClient dikidiHttpClient, BookingCatalogMapper mapper) {
        this.dikidiHttpClient = dikidiHttpClient;
        this.mapper = mapper;
    }

    public List<CategoryDto> getCategories(long companyId) {
        DikidiServicesData data = dikidiHttpClient.get(
                        "/mobile/ajax/newrecord/company_services",
                        Map.of("company", String.valueOf(companyId)),
                        DikidiServicesData.class)
                .resolve(Map.of("COMPANY_ERROR", HttpStatus.NOT_FOUND));
        return mapper.toCategoryDtoList(data);
    }

    public List<LocalDate> getDatesTrue(long companyId, long serviceId, LocalDate from, LocalDate to) {
        DikidiDatesTrue data = dikidiHttpClient.get(
                        "/ajax/newrecord/get_dates_true",
                        Map.of("company_id", String.valueOf(companyId),
                                "services_id[]", String.valueOf(serviceId),
                                "date_from", from.toString(),
                                "date_to", to.toString()),
                        DikidiDatesTrue.class)
                .resolve(Map.of("1", HttpStatus.NOT_FOUND));
        return mapper.toLocalDateList(data);
    }

    public Map<LocalDateTime, List<MasterDto>> getTimes(
            long companyId, long serviceId, LocalDate from, LocalDate to) {
        List<LocalDate> datesTrue = getDatesTrue(companyId, serviceId, from, to);

        List<CompletableFuture<Map<LocalDateTime, List<MasterDto>>>> futures = datesTrue.stream()
                .map(date -> CompletableFuture
                        .supplyAsync(() -> getTimesByDate(companyId, serviceId, date))
                        .exceptionally(ex -> {
                            log.warn("Error getting times: companyId={}, serviceId={}, date={}, error={}",
                                    companyId, serviceId, date, ex.getMessage());
                            return Map.of();
                        }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();


        Map<LocalDateTime, List<MasterDto>> times = new TreeMap<>();

        futures.stream()
                .map(CompletableFuture::join)
                .forEach(dateTimes -> dateTimes.forEach(
                        (dateTime, masters) ->
                                times.computeIfAbsent(dateTime, _ -> new ArrayList<>())
                                        .addAll(masters)
                ));

        return times;
    }

    private Map<LocalDateTime, List<MasterDto>> getTimesByDate(
            long companyId, long serviceId, LocalDate date) {
        DikidiSlotsData data = dikidiHttpClient.get(
                        "/mobile/ajax/newrecord/get_datetimes",
                        Map.of(
                                "company_id", String.valueOf(companyId),
                                "service_id[]", String.valueOf(serviceId),
                                "date", date.toString()),
                        DikidiSlotsData.class)
                .resolve(Map.of("400", HttpStatus.NOT_FOUND));
        try {
            return mapper.toTimeMap(data);
        } catch (DateTimeParseException e) {
            throw new DikidiApiException(
                    HttpStatus.BAD_REQUEST, "INVALID_DATETIME_FORMAT", e.getMessage());
        }
    }
}
