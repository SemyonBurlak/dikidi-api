package io.github.semyonburlak.dikidiapi.client;

import io.github.semyonburlak.dikidiapi.dto.RecordDto;
import io.github.semyonburlak.dikidiapi.dto.SessionCredentials;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecordClientImpl implements RecordClient {

    @Override
    public List<RecordDto> getRecords(SessionCredentials credentials) {
        return List.of();
    }
}
