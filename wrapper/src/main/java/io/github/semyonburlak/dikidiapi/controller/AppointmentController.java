package io.github.semyonburlak.dikidiapi.controller;


import io.github.semyonBurlak.dto.AppointmentPageDto;
import io.github.semyonburlak.dikidiapi.service.AppointmentService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public AppointmentPageDto getAppointments(HttpServletRequest request) {
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

        return appointmentService.getAppointments("token=" + token);
    }
}
