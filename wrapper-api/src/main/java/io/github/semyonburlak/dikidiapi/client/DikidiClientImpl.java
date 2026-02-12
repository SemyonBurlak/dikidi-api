package io.github.semyonburlak.dikidiapi.client;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.semyonburlak.dikidiapi.dto.CategoryDto;
import io.github.semyonburlak.dikidiapi.dto.MasterDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Slf4j
public class DikidiClientImpl implements DikidiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final RateLimiter rateLimiter;

    public DikidiClientImpl(
            RestClient restClient,
            ObjectMapper objectMapper,
            RateLimiterRegistry registry) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.rateLimiter = registry.rateLimiter("dikidi");
    }

    private static final DateTimeFormatter DIKIDI_DATETIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<CategoryDto> getAllCategories(long companyId) {

        Optional<JsonNode> root = executeGet(
                "/mobile/ajax/newrecord/company_services",
                Map.of(
                        "company", companyId
                ),
                "companyId=%d".formatted(companyId)
        );

        if (root.isEmpty()) {
            return List.of();
        }

        JsonNode categoryListNode = root.get().at("/data/list");

        if (categoryListNode.isEmpty() || !categoryListNode.isObject()) {
            log.warn("Category list is empty or not an object: companyId={}", companyId);
            return List.of();
        }

        Map<String, CategoryDto> categoryMap = objectMapper.convertValue(
                categoryListNode, new TypeReference<>() {
                }
        );

        List<CategoryDto> categories = new ArrayList<>(categoryMap.values());

        log.info("Found {} categories", categories.size());
        return categories;
    }

    @Override
    public Map<LocalDateTime, List<MasterDto>> getAllSlots(long companyId, long serviceId) {
        LocalDate now = LocalDate.now();
        List<LocalDate> datesWithSlots = getDatesWithSlots(companyId, serviceId, now, now.plusMonths(1));

        Map<LocalDateTime, List<MasterDto>> slots = new TreeMap<>();

        for (LocalDate date : datesWithSlots) {
            slots.putAll(getSlotsByDay(companyId, serviceId, date));
        }
        log.info("Found {} time slots: companyId={}, serviceId={}", slots.size(), companyId, serviceId);
        return slots;
    }

    private Map<LocalDateTime, List<MasterDto>> getSlotsByDay(long companyId, long serviceId, LocalDate date) {

        Optional<JsonNode> root = executeGet(
                "/mobile/ajax/newrecord/get_datetimes",
                Map.of(
                        "company_id", companyId,
                        "service_id[]", serviceId,
                        "date", date
                ),
                "companyId=%d, serviceId=%d, date=%s"
                        .formatted(companyId, serviceId, date)
        );

        if (root.isEmpty()) {
            return Map.of();
        }

        JsonNode mastersNode = root.get().at("/data/masters");
        JsonNode timesNode = root.get().at("/data/times");

        if (mastersNode.isEmpty() || !mastersNode.isObject()) {

            log.warn("Masters node empty or not an object: companyId={}, serviceId={}, date={}",
                    companyId, serviceId, date
            );

            return Map.of();
        }

        if (timesNode.isEmpty() || !timesNode.isObject()) {

            log.warn("Time node empty or not an object: companyId={}, serviceId={}, date={}",
                    companyId, serviceId, date
            );

            return Map.of();
        }

        Map<Long, MasterDto> masterMap = objectMapper.convertValue(mastersNode, new TypeReference<>() {
        });

        Map<Long, List<String>> timesMap = objectMapper.convertValue(timesNode, new TypeReference<>() {
        });

        Map<LocalDateTime, List<MasterDto>> slots = new TreeMap<>();

        timesMap.forEach((masterId, times) ->
                times.forEach(dateTimeString -> {

                            try {
                                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DIKIDI_DATETIME_FORMATTER);
                                slots.computeIfAbsent(dateTime, _ -> new ArrayList<>())
                                        .add(masterMap.get(masterId));
                            } catch (DateTimeParseException e) {
                                log.warn("Unparseable datetime: '{}'", dateTimeString);
                            }
                        }
                )
        );

        return slots;
    }

    private List<LocalDate> getDatesWithSlots(long companyId, long serviceId, LocalDate from, LocalDate to) {

        Optional<JsonNode> root = executeGet(
                "/ajax/newrecord/get_dates_true",
                Map.of(
                        "company_id", companyId,
                        "services_id[]", serviceId,
                        "date_from", from,
                        "date_to", to
                ),
                "companyId=%d, serviceId=%d, from=%s, to=%s"
                        .formatted(companyId, serviceId, from, to)
        );

        if (root.isEmpty()) {
            return List.of();
        }

        JsonNode datesTrueNode = root.get().path("dates_true");

        if (datesTrueNode.isEmpty() || !datesTrueNode.isArray()) {

            log.warn("dates_true is missing or not an array: companyId={}, serviceId={}, from={}, to={}",
                    companyId, serviceId, from, to
            );

            return List.of();
        }

        List<LocalDate> dates = objectMapper.convertValue(datesTrueNode, new TypeReference<>() {
        });

        log.info("Found {} dates: companyId={}, serviceId={}, from={}, to={}",
                dates.size(), companyId, serviceId, from, to
        );

        return dates;
    }

    private Optional<JsonNode> executeGet(String path, Map<String, Object> queryParams, String context) {
        return rateLimiter.executeSupplier(() -> {
                    try {

                        JsonNode root = restClient.get()
                                .uri(uri -> {
                                    UriBuilder builder = uri.path(path);
                                    queryParams.forEach(builder::queryParam);
                                    return builder.build();
                                })
                                .retrieve()
                                .body(JsonNode.class);

                        if (root == null || root.isEmpty()) {
                            log.warn("Empty response: {}", context);
                            return Optional.empty();
                        }

                        int errorCode = root.at("/error/code").asInt();
                        if (errorCode != 0) {
                            log.warn("Dikidi API error {}: {}", errorCode, context);
                            return Optional.empty();
                        }

                        return Optional.of(root);
                    } catch (RestClientResponseException e) {

                        log.warn(
                                "Dikidi response exception {}: {} body={}",
                                e.getStatusCode(), context, e.getResponseBodyAsString()
                        );

                    } catch (RestClientException e) {
                        log.warn("Dikidi request exception: {}, cause={}", context, e.getMessage());
                    }
                    return Optional.empty();
                }
        );
    }
}

