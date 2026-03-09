package io.github.semyonburlak.wrapper.controller;

import io.github.semyonburlak.wrapper.exception.DikidiApiException;
import io.github.semyonburlak.wrapper.service.AuthService;
import io.github.semyonburlak.wrapper.support.WrapperTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(WrapperTestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void loginSuccessReturns200WithSetCookieHeader() throws Exception {
        when(authService.login(any())).thenReturn("token=abc; Path=/");

        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user&password=pass"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, "token=abc; Path=/"));
    }

    @Test
    void loginBadDataErrorReturns400WithJsonBody() throws Exception {
        when(authService.login(any())).thenThrow(
                new DikidiApiException(HttpStatus.BAD_REQUEST,
                        "USER_AUTHORISATION_DATA_ERROR", "Bad data"));

        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user&password=pass"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_AUTHORISATION_DATA_ERROR"))
                .andExpect(jsonPath("$.message").value("Bad data"));
    }

    @Test
    void loginAuthErrorReturns401WithJsonBody() throws Exception {
        when(authService.login(any())).thenThrow(
                new DikidiApiException(HttpStatus.UNAUTHORIZED,
                        "USER_AUTHORISATION_ERROR", "Bad credentials"));

        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user&password=pass"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("USER_AUTHORISATION_ERROR"));
    }
}
