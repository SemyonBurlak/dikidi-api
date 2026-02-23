package io.github.semyonburlak.dikidiapi.client;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.semyonburlak.dikidiapi.dto.CategoryDto;
import io.github.semyonburlak.dikidiapi.dto.MasterDto;
import io.github.semyonburlak.dikidiapi.exception.DikidiApiException;
import io.github.semyonburlak.dikidiapi.exception.DikidiErrorCode;
import io.github.semyonburlak.dikidiapi.exception.DikidiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.github.semyonburlak.dikidiapi.client.ErrorParser.parseHttpError;

@Service
@Slf4j
public class DikidiClientImpl implements DikidiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final RateLimiter rateLimiter;

    private static final DateTimeFormatter DIKIDI_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DikidiClientImpl(
            RestClient restClient,
            ObjectMapper objectMapper,
            RateLimiterRegistry registry) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.rateLimiter = registry.rateLimiter("dikidi");
    }

    @Override
    public List<CategoryDto> getAllCategories(long companyId) {
        JsonNode root = executeGet(
                "/mobile/ajax/newrecord/company_services",
                Map.of("company", companyId),
                "getAllCategories: companyId=%d".formatted(companyId)
        );

        JsonNode listNode = root.at("/data/list");
        if (listNode.isMissingNode() || listNode.isEmpty()) {
            log.warn("Category list is empty: companyId={}", companyId);
            return List.of();
        }

        Map<String, CategoryDto> categoryMap = objectMapper.convertValue(
                listNode, new TypeReference<>() {
                }
        );

        List<CategoryDto> categories = new ArrayList<>(categoryMap.values());
        log.info("Found {} categories: companyId={}", categories.size(), companyId);
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
        JsonNode root = executeGet(
                "/mobile/ajax/newrecord/get_datetimes",
                Map.of(
                        "company_id", companyId,
                        "service_id[]", serviceId,
                        "date", date
                ),
                "getSlotsByDay: companyId=%d, serviceId=%d, date=%s"
                        .formatted(companyId, serviceId, date)
        );

        JsonNode mastersNode = root.at("/data/masters");
        JsonNode timesNode = root.at("/data/times");

        if (mastersNode.isMissingNode() || timesNode.isMissingNode()) {
            log.warn("Masters or times missing: companyId={}, serviceId={}, date={}",
                    companyId, serviceId, date);
            return Map.of();
        }

        Map<Long, MasterDto> masterMap = objectMapper.convertValue(
                mastersNode, new TypeReference<>() {
                }
        );
        Map<Long, List<String>> timesMap = objectMapper.convertValue(
                timesNode, new TypeReference<>() {
                }
        );

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
                })
        );

        return slots;
    }

    private List<LocalDate> getDatesWithSlots(long companyId, long serviceId,
                                              LocalDate from, LocalDate to) {
        JsonNode root = executeGet(
                "/ajax/newrecord/get_dates_true",
                Map.of(
                        "company_id", companyId,
                        "services_id[]", serviceId,
                        "date_from", from,
                        "date_to", to
                ),
                "getDatesWithSlots: companyId=%d, serviceId=%d, from=%s, to=%s"
                        .formatted(companyId, serviceId, from, to)
        );

        JsonNode datesTrueNode = root.path("dates_true");
        if (datesTrueNode.isMissingNode() || !datesTrueNode.isArray()) {
            log.warn("dates_true missing: companyId={}, serviceId={}", companyId, serviceId);
            return List.of();
        }

        List<LocalDate> dates = objectMapper.convertValue(
                datesTrueNode, new TypeReference<>() {
                }
        );

        log.info("Found {} dates: companyId={}, serviceId={}", dates.size(), companyId, serviceId);
        return dates;
    }

    private JsonNode executeGet(String path, Map<String, Object> queryParams, String context) {
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
                    throw new DikidiApiException(DikidiErrorCode.EMPTY_RESPONSE, "No body");
                }

                checkForError(root);
                return root;
            } catch (HttpClientErrorException e) {
                log.warn("HTTP error {}: {}", e.getStatusCode().value(), context);
                throw parseHttpError(e, objectMapper);
            } catch (RestClientException e) {
                log.warn("Request failed: {}", context);
                throw new DikidiException("Request failed", e);
            }
        });
    }

    private void checkForError(JsonNode root) {
        JsonNode errorNode = root.path("error");

        JsonNode codeNode = errorNode.path("code");

        if (codeNode.isMissingNode()) return;

        boolean hasError = codeNode.isNumber()
                ? codeNode.asInt() != 0
                : !codeNode.asString().isEmpty() && !codeNode.asString().equals("null");

        if (hasError) {
            throw new DikidiApiException(errorNode);
        }
    }
}