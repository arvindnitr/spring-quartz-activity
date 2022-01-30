package com.arvind.quartz.scheduler.quartz.job;

import com.arvind.quartz.scheduler.dto.TimePrintingDetails;
import com.arvind.quartz.scheduler.service.TimePrintingService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class TimePrintingJob extends QuartzJobBean {

    @Autowired
    private TimePrintingService timePrintingService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Long time = jobDataMap.getLong("time");
        timePrintingService.printTime(TimePrintingDetails.builder().time(time).build());
        log.info("Next fire time:" + context.getTrigger().getNextFireTime());
        context.getJobDetail().getJobDataMap().put("time", System.currentTimeMillis());
    }


}
