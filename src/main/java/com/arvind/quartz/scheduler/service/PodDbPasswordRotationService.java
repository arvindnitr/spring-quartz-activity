package com.arvind.quartz.scheduler.service;

import com.arvind.quartz.scheduler.activity.Activity;
import com.arvind.quartz.scheduler.activity.ActivityContext;
import com.arvind.quartz.scheduler.activity.Operation;
import com.arvind.quartz.scheduler.activity.ResourceType;
import com.arvind.quartz.scheduler.dto.PodDbDetails;
import com.arvind.quartz.scheduler.dto.StandardResponse;
import com.arvind.quartz.scheduler.quartz.job.PodDbPasswordRotationJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PodDbPasswordRotationService {

    @Autowired
    private Scheduler scheduler;

    public StandardResponse schedulePodDbPasswordRotation(PodDbDetails podDbDetails) {
        StandardResponse response;
                JobDetail jobDetail = podDbPasswordRotationJobDetail(podDbDetails);
        Trigger trigger = podDbPasswordRotationTrigger(jobDetail, podDbDetails);
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            response = new StandardResponse(true, jobDetail.getKey().getName() , jobDetail.getKey().getGroup(), "password change scheduled successfully");
        } catch (SchedulerException e) {
            log.error("Exception while scheduling podDB password rotation:" + e);
            response = new StandardResponse(false, "Exception while scheduling podDB password rotation, please try again later...");
        }
        return response;
    }

    private JobDetail podDbPasswordRotationJobDetail(PodDbDetails podDbDetails) {

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("newPassword", podDbDetails.getNewPassword());
        jobDataMap.put("dbaPassword", podDbDetails.getDbaPassword());
        jobDataMap.put("schema", podDbDetails.getSchema());
        jobDataMap.put("region", podDbDetails.getRegion());
        jobDataMap.put("podNumber", podDbDetails.getPodNumber());

        return JobBuilder.newJob(PodDbPasswordRotationJob.class).withIdentity(UUID.randomUUID().toString(),"PodDBPasswordRotation").
                withDescription("Pod Db password rotation adhoc job").setJobData(jobDataMap).storeDurably().requestRecovery().build();
    }


    private Trigger podDbPasswordRotationTrigger(JobDetail jobDetail, PodDbDetails podDbDetails) {
        ZonedDateTime startAt = ZonedDateTime.of(podDbDetails.getDateTime(), podDbDetails.getTimeZone());
        return TriggerBuilder.newTrigger().withIdentity(jobDetail.getKey().getName(), "podDB-password-rotation-triggers").
                withDescription("Pod Db password rotation triggers").startAt(Date.from(startAt.toInstant())).
                withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow()).
                build();
    }

    @Activity(subject = ResourceType.MT_DB, operation = Operation.PASSWORD_CHANGE)
    public void changeDbPassword(@ActivityContext(subject = "podNumber", participants = "region,schema")PodDbDetails podDbDetails) {
        log.info("Changing pod{} password for schema {} in region {}", podDbDetails.getPodNumber(), podDbDetails.getSchema(), podDbDetails.getRegion());
    }
}
