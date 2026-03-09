package io.github.semyonburlak.wrapper.controller;

import io.github.semyonburlak.dto.CategoryDto;
import io.github.semyonburlak.dto.ServiceDto;
import io.github.semyonburlak.wrapper.exception.DikidiApiException;
import io.github.semyonburlak.wrapper.service.BookingCatalogService;
import io.github.semyonburlak.wrapper.support.WrapperTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingCatalogController.class)
@Import(WrapperTestConfig.class)
class BookingCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingCatalogService bookingCatalogService;

    @Test
    void getCategoriesSuccessReturns200WithJsonArray() throws Exception {
        List<CategoryDto> categories = List.of(
                new CategoryDto(1L, "Hair", List.of(new ServiceDto(101L, "Cut"))));
        when(bookingCatalogService.getCategories(1L)).thenReturn(categories);

        mockMvc.perform(get("/catalog/companies/1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Hair"));
    }

    @Test
    void getCategoriesCompanyErrorReturns404WithJsonBody() throws Exception {
        when(bookingCatalogService.getCategories(anyLong())).thenThrow(
                new DikidiApiException(HttpStatus.NOT_FOUND, "COMPANY_ERROR", "Not found"));

        mockMvc.perform(get("/catalog/companies/1/categories"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMPANY_ERROR"))
                .andExpect(jsonPath("$.message").value("Not found"));
    }

    @Test
    void getAllTimesSuccessReturns200() throws Exception {
        when(bookingCatalogService.getTimes(anyLong(), anyLong(), any(), any()))
                .thenReturn(Map.of());

        mockMvc.perform(get("/catalog/companies/1/services/2/times"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllTimesServiceErrorReturns502() throws Exception {
        when(bookingCatalogService.getTimes(anyLong(), anyLong(), any(), any())).thenThrow(
                new DikidiApiException(HttpStatus.BAD_GATEWAY, "NETWORK", "timeout"));

        mockMvc.perform(get("/catalog/companies/1/services/2/times"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("NETWORK"));
    }
}
