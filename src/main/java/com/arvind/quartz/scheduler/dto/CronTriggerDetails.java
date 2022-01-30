package com.arvind.quartz.scheduler.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class CronTriggerDetails {

    private String cronExpression;
    private Date startTime;
    private Date endTime;
    private String timeZone;
}
