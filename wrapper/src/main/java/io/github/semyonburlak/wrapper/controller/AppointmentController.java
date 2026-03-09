package io.github.semyonburlak.wrapper.controller;


import io.github.semyonburlak.dto.AppointmentPageDto;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiRecordsData;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiTimeReservation;
import io.github.semyonburlak.wrapper.service.AppointmentService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/my")
    public AppointmentPageDto getAppointments(HttpServletRequest request) {
        String token = extractToken(request);

        return appointmentService.getAppointments("token=" + token);
    }


    @GetMapping("/reservation")
    public DikidiTimeReservation getTimeReservation(
            @RequestParam Long companyId,
            @RequestParam Long masterId,
            @RequestParam Long serviceId,
            @RequestParam LocalDateTime time
    ) {

        return appointmentService.getTimeReservation(companyId, masterId, serviceId, time);
    }

    @GetMapping("/records_info")
    public DikidiRecordsData getRecordsInfo(
            @RequestParam Long companyId,
            @RequestParam Long recordId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);

        return appointmentService.getRecordsData(companyId, recordId, token);
    }

    private static @NonNull String extractToken(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals("token"))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return token;
    }
}
