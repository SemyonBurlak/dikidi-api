package io.github.semyonburlak.dikidiapi.service;

import io.github.semyonburlak.dikidiapi.client.DikidiHttpClient;
import io.github.semyonburlak.dikidiapi.dto.DikidiResponse;
import io.github.semyonburlak.dikidiapi.dto.dikidi.AuthCallback;
import io.github.semyonburlak.dikidiapi.exception.DikidiApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final DikidiHttpClient authHttpClient;

    public String login(MultiValueMap<String, String> body) {

        DikidiResponse<AuthCallback> response = authHttpClient.post(
                "/ajax/user/auth",
                body,
                AuthCallback.class
        );

        response.resolve(
                Map.of(
                        "USER_AUTHORISATION_DATA_ERROR", HttpStatus.BAD_REQUEST,
                        "USER_AUTHORISATION_ERROR", HttpStatus.UNAUTHORIZED,
                        "NUMBER_NOT_TRUE", HttpStatus.BAD_REQUEST
                )
        );

        List<String> cookieHeader = response.cookies();

        if (cookieHeader == null || cookieHeader.isEmpty()) {
            throw new DikidiApiException(HttpStatus.BAD_GATEWAY, "COOKIES_DID_NOT_RETURN", "No cookies were returned");
        }

        Optional<String> token = extractToken(cookieHeader);

        if (token.isEmpty()) {
            throw new DikidiApiException(HttpStatus.BAD_GATEWAY, "NO_TOKEN", "No token cookie in response");
        }

        return token.get();
    }

    private Optional<String> extractToken(List<String> setCookies) {
        return setCookies.stream()
                .filter(cookie -> cookie.trim().startsWith("token="))
                .findFirst();
    }
}
