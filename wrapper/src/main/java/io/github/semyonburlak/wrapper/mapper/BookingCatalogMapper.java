package io.github.semyonburlak.wrapper.mapper;

import io.github.semyonburlak.dto.CategoryDto;
import io.github.semyonburlak.dto.MasterDto;
import io.github.semyonburlak.dto.ServiceDto;
import io.github.semyonburlak.wrapper.dto.dikidi.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
@Slf4j
public class BookingCatalogMapper {

    private static final DateTimeFormatter SLOT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<CategoryDto> toCategoryDtoList(DikidiServicesData data) {
        if (data == null || data.list() == null) {
            log.warn("ServicesData empty");
            return List.of();
        }
        List<CategoryDto> list = data.categories().stream().map(this::toCategoryDto).toList();
        log.info("Mapped {} categories", list.size());
        return list;
    }

    private CategoryDto toCategoryDto(DikidiCategory category) {
        List<ServiceDto> services = category.services().stream().map(this::toServiceDto).toList();
        return new CategoryDto(category.id(), category.name(), services);
    }

    private ServiceDto toServiceDto(DikidiService service) {
        return new ServiceDto(service.id(), service.name());
    }

    public List<LocalDate> toLocalDateList(DikidiDatesTrue dikidiDatesTrue) {
        if (dikidiDatesTrue == null || dikidiDatesTrue.datesTrue() == null) {
            log.warn("DatesTrue empty");
            return List.of();
        }
        List<LocalDate> list = dikidiDatesTrue.datesTrue().stream().map(LocalDate::parse).toList();
        log.info("Mapped {} available dates", list.size());
        return list;
    }

    public Map<LocalDateTime, List<MasterDto>> toTimeMap(DikidiSlotsData data) {
        if (data == null || data.masters() == null || data.times() == null) {
            log.warn("SlotsData empty");
            return Map.of();
        }

        Map<LocalDateTime, List<MasterDto>> slots = new TreeMap<>();
        data.times().forEach((masterId, times) -> addMasterSlots(data, masterId, times, slots));
        log.info("Mapped {} time slots for {} masters", slots.size(), data.masters().size());
        return slots;
    }

    private void addMasterSlots(
            DikidiSlotsData data,
            String masterId,
            List<String> times,
            Map<LocalDateTime, List<MasterDto>> slots) {
        DikidiMaster master = data.masters().get(masterId);
        if (master == null) {
            return;
        }
        MasterDto masterDto = new MasterDto(master.id(), master.username());
        times.forEach(s -> addSlot(slots, LocalDateTime.parse(s, SLOT_FORMAT), masterDto));
    }

    private void addSlot(
            Map<LocalDateTime, List<MasterDto>> slots, LocalDateTime dateTime, MasterDto masterDto
    ) {
        slots.computeIfAbsent(dateTime, _ -> new ArrayList<>()).add(masterDto);
    }
}
