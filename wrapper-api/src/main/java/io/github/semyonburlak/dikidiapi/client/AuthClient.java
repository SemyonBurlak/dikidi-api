package io.github.semyonburlak.dikidiapi.client;

import io.github.semyonburlak.dikidiapi.dto.AuthResult;

public interface AuthClient {
    AuthResult authenticate(String number, String password);
}
