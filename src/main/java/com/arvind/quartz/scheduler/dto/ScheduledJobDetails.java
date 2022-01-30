package com.arvind.quartz.scheduler.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScheduledJobDetails {

    String groupName;
    String jobName;
    List<String> triggerTimeDetails;
}
