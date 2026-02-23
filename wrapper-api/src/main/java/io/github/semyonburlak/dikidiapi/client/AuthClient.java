package io.github.semyonburlak.dikidiapi.client;

import io.github.semyonburlak.dikidiapi.dto.AuthResult;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public interface AuthClient {
    AuthResult authenticate(MultiValueMap<String, String> body);
}
