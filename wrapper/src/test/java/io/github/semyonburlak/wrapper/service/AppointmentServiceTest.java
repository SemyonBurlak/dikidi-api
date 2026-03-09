package io.github.semyonburlak.wrapper.service;

import io.github.semyonburlak.dto.AppointmentPageDto;
import io.github.semyonburlak.wrapper.client.DikidiHttpClient;
import io.github.semyonburlak.wrapper.dto.DikidiResponse;
import io.github.semyonburlak.wrapper.dto.DikidiResult;
import io.github.semyonburlak.wrapper.dto.dikidi.DikidiTimeReservation;
import io.github.semyonburlak.wrapper.exception.DikidiApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private DikidiHttpClient dikidiHttpClient;

    @InjectMocks
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(appointmentService, "dateTimePattern", "yyyy-MM-dd HH:mm:ss");
    }

    @Test
    void getAppointmentsSuccessReturnsDto() {
        AppointmentPageDto expected = new AppointmentPageDto(false, List.of());
        DikidiResponse<AppointmentPageDto> response = DikidiResponse.of(
                DikidiResult.ok(expected), null);
        doReturn(response).when(dikidiHttpClient).get(anyString(), any(), anyString(), any());

        AppointmentPageDto result = appointmentService.getAppointments("token=abc");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAppointmentsNetworkErrorThrowsWith503() {
        DikidiResponse<AppointmentPageDto> response = DikidiResponse.of(
                DikidiResult.fail("NETWORK", "connection refused"), null);
        doReturn(response).when(dikidiHttpClient).get(anyString(), any(), anyString(), any());

        assertThatThrownBy(() -> appointmentService.getAppointments("token=abc"))
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void getAppointmentsExpiredTokenThrowsWith401() {
        DikidiResponse<AppointmentPageDto> response = DikidiResponse.of(
                DikidiResult.fail("400", "Authorization required"), null);
        doReturn(response).when(dikidiHttpClient).get(anyString(), any(), anyString(), any());

        assertThatThrownBy(() -> appointmentService.getAppointments("token=expired"))
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void getTimeReservationSuccessReturnsDto() {
        DikidiTimeReservation expected = new DikidiTimeReservation(1L, 2L, "1:00");
        DikidiResponse<DikidiTimeReservation> response = DikidiResponse.of(
                DikidiResult.ok(expected), null);
        doReturn(response).when(dikidiHttpClient).get(anyString(), any(), any());

        DikidiTimeReservation result = appointmentService.getTimeReservation(
                1L, 2L, 3L, LocalDateTime.of(2024, 1, 15, 10, 0));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getTimeReservationSpecialistNotFoundThrowsWith404() {
        DikidiResponse<DikidiTimeReservation> response = DikidiResponse.of(
                DikidiResult.fail("1", "Specialist not found"), null);
        doReturn(response).when(dikidiHttpClient).get(anyString(), any(), any());

        assertThatThrownBy(() -> appointmentService.getTimeReservation(
                940010L, 0L, 9190182L, LocalDateTime.of(2026, 4, 1, 10, 0, 0)))
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getTimeReservationCompanyErrorThrowsWith404() {
        DikidiResponse<DikidiTimeReservation> response = DikidiResponse.of(
                DikidiResult.fail("COMPANY_ERROR", "Company not found"), null);
        doReturn(response).when(dikidiHttpClient).get(anyString(), any(), any());

        assertThatThrownBy(() -> appointmentService.getTimeReservation(
                0L, 0L, 0L, LocalDateTime.of(2026, 4, 1, 10, 0, 0)))
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
