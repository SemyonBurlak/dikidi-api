package io.github.semyonburlak.dikidiapi.controller;

import io.github.semyonBurlak.dto.AuthResult;
import io.github.semyonburlak.dikidiapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<AuthResult> login(@RequestBody MultiValueMap<String, String> body) {
        String token = authService.login(body);
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.SET_COOKIE, List.of(token));
        return ResponseEntity.ok().headers(headers).build();
    }


}
