package io.github.semyonburlak.wrapper.controller;

import io.github.semyonburlak.dto.AppointmentPageDto;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiTimeReservation;
import io.github.semyonburlak.wrapper.exception.DikidiApiException;
import io.github.semyonburlak.wrapper.service.AppointmentService;
import io.github.semyonburlak.wrapper.support.WrapperTestConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@Import(WrapperTestConfig.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    @Test
    void getAppointmentsWithoutCookieReturns401() throws Exception {
        mockMvc.perform(get("/appointments/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAppointmentsWithTokenCookieCallsServiceAndReturns200() throws Exception {
        AppointmentPageDto dto = new AppointmentPageDto(false, List.of());
        when(appointmentService.getAppointments("token=abc")).thenReturn(dto);

        mockMvc.perform(get("/appointments/my").cookie(new Cookie("token", "abc")))
                .andExpect(status().isOk());
    }

    @Test
    void getAppointmentsServiceErrorReturns502WithJsonBody() throws Exception {
        when(appointmentService.getAppointments(any())).thenThrow(
                new DikidiApiException(HttpStatus.BAD_GATEWAY, "NULL_ROOT", "no response"));

        mockMvc.perform(get("/appointments/my").cookie(new Cookie("token", "abc")))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("NULL_ROOT"));
    }

    @Test
    void getTimeReservationReturns200() throws Exception {
        DikidiTimeReservation dto = new DikidiTimeReservation(1L, 2L, "1:00");
        when(appointmentService.getTimeReservation(eq(1L), eq(2L), eq(3L), any(LocalDateTime.class)))
                .thenReturn(dto);

        mockMvc.perform(get("/appointments/reservation")
                        .param("companyId", "1")
                        .param("masterId", "2")
                        .param("serviceId", "3")
                        .param("time", "2024-01-15 10:00:00"))
                .andExpect(status().isOk());
    }

    @Test
    void getTimeReservationWithInvalidParamsReturns400() throws Exception {
        mockMvc.perform(get("/appointments/reservation")
                        .param("companyId", "1"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAppointmentsOtherCookiesPresentButNoTokenReturns401() throws Exception {
        mockMvc.perform(get("/appointments/my").cookie(new Cookie("session", "xyz")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTimeReservationServiceErrorReturns502() throws Exception {
        when(appointmentService.getTimeReservation(anyLong(), anyLong(), anyLong(), any()))
                .thenThrow(new DikidiApiException(HttpStatus.BAD_GATEWAY, "NULL_ROOT", "error"));

        mockMvc.perform(get("/appointments/reservation")
                        .param("companyId", "1")
                        .param("masterId", "2")
                        .param("serviceId", "3")
                        .param("time", "2024-01-15 10:00:00"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("NULL_ROOT"));
    }
}
