package io.github.semyonBurlak.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record RecordDto(
        long id,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime time,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timeTo,
        CompanyDto company,
        List<ServiceDto> services,
        List<MasterDto> employees
) { }
