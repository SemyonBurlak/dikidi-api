package io.github.semyonburlak.dikidiapi.controller;

import io.github.semyonburlak.dikidiapi.client.DikidiClient;
import io.github.semyonburlak.dikidiapi.dto.CategoryDto;
import io.github.semyonburlak.dikidiapi.dto.MasterDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class DikidiRestController {

    private final DikidiClient dikidiClient;

    @GetMapping("/companies/{companyId}/categories")
    public List<CategoryDto> getCategories(@PathVariable long companyId) {
        return dikidiClient.getAllCategories(companyId);
    }

    @GetMapping("/companies/{companyId}/services/{serviceId}/slots")
    public Map<LocalDateTime, List<MasterDto>> getAllTimes(@PathVariable long companyId, @PathVariable long serviceId) {
        return dikidiClient.getAllSlots(companyId, serviceId);
    }

}
