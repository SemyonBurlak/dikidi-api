package io.github.semyonburlak.dikidiapi.client;

import io.github.semyonburlak.dikidiapi.dto.CategoryDto;
import io.github.semyonburlak.dikidiapi.dto.MasterDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DikidiClient {

    List<CategoryDto> getAllCategories(long companyId);

    Map<LocalDateTime, List<MasterDto>> getAllSlots(long companyId, long serviceId);
}
