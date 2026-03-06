package io.github.semyonburlak.dikidiapi.controller;

import io.github.semyonBurlak.dto.CategoryDto;
import io.github.semyonBurlak.dto.MasterDto;
import io.github.semyonburlak.dikidiapi.service.BookingCatalogService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/catalog")
public class BookingCatalogController {

    private final BookingCatalogService bookingCatalogService;

    @GetMapping("/companies/{companyId}/categories")
    public List<CategoryDto> getCategories(@PathVariable long companyId) {
        return bookingCatalogService.getCategories(companyId);
    }

    @GetMapping("/companies/{companyId}/services/{serviceId}/times")
    public Map<LocalDateTime, List<MasterDto>> getAllTimes(
            @PathVariable long companyId, @PathVariable long serviceId) {
        LocalDate now = LocalDate.now();
        return bookingCatalogService.getTimes(companyId, serviceId, now, now.plusMonths(6));
    }
}
