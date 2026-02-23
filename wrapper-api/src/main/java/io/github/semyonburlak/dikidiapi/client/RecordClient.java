package io.github.semyonburlak.dikidiapi.client;

import io.github.semyonburlak.dikidiapi.dto.RecordDto;
import io.github.semyonburlak.dikidiapi.dto.SessionCredentials;

import java.util.List;

public interface RecordClient {
    List<RecordDto> getRecords(SessionCredentials credentials);
}
