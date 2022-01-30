package com.arvind.quartz.scheduler.quartz.job;

import com.arvind.quartz.scheduler.dto.PodDbDetails;
import com.arvind.quartz.scheduler.service.PodDbPasswordRotationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PodDbPasswordRotationJob extends QuartzJobBean {

    @Autowired
    private PodDbPasswordRotationService podDbPasswordRotationService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        String dbaPassword = jobDataMap.getString("dbaPassword");
        String schema = jobDataMap.getString("schema");
        String newPassword = jobDataMap.getString("newPassword");
        String region = jobDataMap.getString("region");
        int podNumber = jobDataMap.getInt("podNumber");

        PodDbDetails podDbDetails = PodDbDetails.builder().dbaPassword(dbaPassword).newPassword(newPassword).
                region(region).podNumber(podNumber).schema(schema).build();

        podDbPasswordRotationService.changeDbPassword(podDbDetails);
        log.info("Next fire time:" + context.getTrigger().getNextFireTime());
    }
}
