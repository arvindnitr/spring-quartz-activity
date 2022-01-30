package com.arvind.quartz.scheduler.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class ScheduleDetails {

    String name;
    String instanceId;
    int numberOfJobs;
    Date runningSince;
    List<ScheduledJobDetails> scheduledJobDetails;
    Set<String> pausedTriggerGroups;
    List<String> currentlyExecutingJobs;
}
