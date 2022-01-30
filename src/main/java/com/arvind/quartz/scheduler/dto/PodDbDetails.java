package com.arvind.quartz.scheduler.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
public class PodDbDetails {

    @NotEmpty
    private String dbaPassword;
    @NotEmpty
    private String newPassword;
    @NotEmpty
    private String region;
    @NotNull
    private Integer podNumber;
    @NotNull
    private String schema;
    @NotNull
    private LocalDateTime dateTime;
    @NotNull
    private ZoneId timeZone;
}
