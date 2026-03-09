package io.github.semyonburlak.wrapper.service;


import io.github.semyonburlak.dto.AppointmentPageDto;
import io.github.semyonburlak.wrapper.client.DikidiHttpClient;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiRecordsData;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiTimeReservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    @Value("${spring.mvc.format.date-time}")
    private String dateTimePattern;

    private final DikidiHttpClient dikidiHttpClient;

    public AppointmentPageDto getAppointments(String token) {
        return dikidiHttpClient.get(
                "/mobile/ajax/newrecord/get_records",
                Map.of("fresh", "new"),
                token,
                AppointmentPageDto.class
        ).resolve(Map.of("400", HttpStatus.UNAUTHORIZED));
    }

    public DikidiTimeReservation getTimeReservation(
            Long companyId,
            Long masterId,
            Long serviceId,
            LocalDateTime time
    ) {
        return dikidiHttpClient.get(
                "/ajax/newrecord/time_reservation",
                Map.of(
                        "company_id", String.valueOf(companyId),
                        "master_id", String.valueOf(masterId),
                        "services_id[]", String.valueOf(serviceId),
                        "time", time.format(DateTimeFormatter.ofPattern(dateTimePattern))
                ),
                DikidiTimeReservation.class
        ).resolve(Map.of(
                "1", HttpStatus.NOT_FOUND,
                "COMPANY_ERROR", HttpStatus.NOT_FOUND
        ));
    }

    public DikidiRecordsData getRecordsData(
            Long companyId,
            Long recordId,
            String token
    ) {
        return dikidiHttpClient.get(
                "/mobile/ajax/newrecord/records_info",
                Map.of(
                        "companyId", String.valueOf(companyId),
                        "record_id_list[]", String.valueOf(recordId)
                ),
                token,
                DikidiRecordsData.class
        ).resolve();
    }
}
