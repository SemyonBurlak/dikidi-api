package io.github.semyonburlak.dikidiapi.service;

import io.github.semyonburlak.dikidiapi.client.GetClient;
import io.github.semyonburlak.dikidiapi.dto.AuthResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final GetClient client;



}
