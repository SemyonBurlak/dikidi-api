package io.github.semyonburlak.wrapper.service;

import io.github.semyonburlak.wrapper.client.DikidiHttpClient;
import io.github.semyonburlak.wrapper.dto.DikidiResponse;
import io.github.semyonburlak.wrapper.dto.DikidiResult;
import io.github.semyonburlak.wrapper.dto.dikidi.AuthCallback;
import io.github.semyonburlak.wrapper.exception.DikidiApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private DikidiHttpClient authHttpClient;

    @InjectMocks
    private AuthService authService;

    private DikidiResponse<AuthCallback> okResponseWithCookies(List<String> cookies) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.SET_COOKIE, cookies);
        return DikidiResponse.of(DikidiResult.ok(new AuthCallback(null)), headers);
    }

    @Test
    void loginSuccessReturnsTokenCookie() {
        DikidiResponse<AuthCallback> response = okResponseWithCookies(List.of("token=abc; Path=/"));
        doReturn(response).when(authHttpClient).post(anyString(), any(), any());

        String result = authService.login(new LinkedMultiValueMap<>());

        assertThat(result).isEqualTo("token=abc; Path=/");
    }

    @Test
    void loginTokenNotFirstCookieExtractsCorrectly() {
        DikidiResponse<AuthCallback> response = okResponseWithCookies(
                List.of("session=xyz; Path=/", "token=abc; Path=/"));
        doReturn(response).when(authHttpClient).post(anyString(), any(), any());

        String result = authService.login(new LinkedMultiValueMap<>());

        assertThat(result).isEqualTo("token=abc; Path=/");
    }

    @Test
    void loginNoCookiesReturnedThrowsCookiesDidNotReturn() {
        DikidiResponse<AuthCallback> response = DikidiResponse.of(
                DikidiResult.ok(new AuthCallback(null)), null);
        doReturn(response).when(authHttpClient).post(anyString(), any(), any());

        assertThatThrownBy(() -> authService.login(new LinkedMultiValueMap<>()))
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> {
                    DikidiApiException ex = (DikidiApiException) e;
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(ex.getCode()).isEqualTo("COOKIES_DID_NOT_RETURN");
                });
    }

    @Test
    void loginCookiesWithoutTokenThrowsNoToken() {
        DikidiResponse<AuthCallback> response = okResponseWithCookies(List.of("session=xyz; Path=/"));
        doReturn(response).when(authHttpClient).post(anyString(), any(), any());

        assertThatThrownBy(() -> authService.login(new LinkedMultiValueMap<>()))
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> {
                    DikidiApiException ex = (DikidiApiException) e;
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(ex.getCode()).isEqualTo("NO_TOKEN");
                });
    }

    @Test
    void loginDikidiUserAuthErrorThrowsUnauthorized() {
        DikidiResponse<AuthCallback> response = DikidiResponse.of(
                DikidiResult.fail("USER_AUTHORISATION_ERROR", "wrong credentials"), null);
        doReturn(response).when(authHttpClient).post(anyString(), any(), any());

        assertThatThrownBy(() -> authService.login(new LinkedMultiValueMap<>()))
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void loginDikidiDataErrorThrowsBadRequest() {
        DikidiResponse<AuthCallback> response = DikidiResponse.of(
                DikidiResult.fail("USER_AUTHORISATION_DATA_ERROR", "bad data"), null);
        doReturn(response).when(authHttpClient).post(anyString(), any(), any());

        assertThatThrownBy(() -> authService.login(new LinkedMultiValueMap<>()))
                .isInstanceOf(DikidiApiException.class)
                .satisfies(e -> assertThat(((DikidiApiException) e).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
