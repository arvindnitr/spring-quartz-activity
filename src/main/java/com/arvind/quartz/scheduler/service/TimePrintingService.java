package com.arvind.quartz.scheduler.service;

import com.arvind.quartz.scheduler.dto.TimePrintingDetails;
import com.arvind.quartz.scheduler.activity.Activity;
import com.arvind.quartz.scheduler.activity.ActivityContext;
import com.arvind.quartz.scheduler.activity.ResourceType;
import com.arvind.quartz.scheduler.activity.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TimePrintingService {

    @Activity(subject = ResourceType.TIME, operation = Operation.PRINT)
    public void printTime(@ActivityContext(subject = "time") TimePrintingDetails timePrintingDetails) {
        log.info("Time to print - {}", timePrintingDetails.getTime());
    }
}
