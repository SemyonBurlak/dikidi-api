package io.github.semyonburlak.dikidiapi.service;


import io.github.semyonBurlak.dto.AppointmentPageDto;
import io.github.semyonburlak.dikidiapi.client.DikidiHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final DikidiHttpClient dikidiHttpClient;

    public AppointmentPageDto getAppointments(String token) {
        return dikidiHttpClient.get(
                "/mobile/ajax/newrecord/get_records",
                Map.of("fresh", "new"),
                token,
                AppointmentPageDto.class
        ).resolve();
    }
}
