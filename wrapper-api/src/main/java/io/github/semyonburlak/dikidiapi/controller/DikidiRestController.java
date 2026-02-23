package io.github.semyonburlak.dikidiapi.controller;

import io.github.semyonburlak.dikidiapi.client.AuthClient;
import io.github.semyonburlak.dikidiapi.client.DikidiClient;
import io.github.semyonburlak.dikidiapi.dto.AuthResult;
import io.github.semyonburlak.dikidiapi.dto.CategoryDto;
import io.github.semyonburlak.dikidiapi.dto.MasterDto;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class DikidiRestController {

    private final DikidiClient dikidiClient;
    private final AuthClient authClient;

    @GetMapping("/companies/{companyId}/categories")
    public List<CategoryDto> getCategories(@PathVariable long companyId) {
        return dikidiClient.getAllCategories(companyId);
    }

    @GetMapping("/companies/{companyId}/services/{serviceId}/slots")
    public Map<LocalDateTime, List<MasterDto>> getAllTimes(@PathVariable long companyId, @PathVariable long serviceId) {
        return dikidiClient.getAllSlots(companyId, serviceId);
    }

    @PostMapping(
            value = "/auth",
            consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE }
    )
    public AuthResult authenticate(@RequestParam MultiValueMap<String, String> body) {
        return authClient.authenticate(body);
    }

}
