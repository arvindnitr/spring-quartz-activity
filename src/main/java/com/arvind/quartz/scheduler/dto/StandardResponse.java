package com.arvind.quartz.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class StandardResponse {

    private boolean success;
    private String jobId;
    private String jobGroup;
    private String message;

    public StandardResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
